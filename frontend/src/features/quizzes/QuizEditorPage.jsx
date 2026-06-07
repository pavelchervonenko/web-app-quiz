import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { ArrowLeft, Play, Plus, Save, Trash2 } from 'lucide-react';
import {
  addQuestion,
  deleteQuestion,
  deleteQuiz,
  getQuiz,
  updateQuestion,
  updateQuiz,
} from '../../shared/api/quizApi.js';
import { startSession } from '../../shared/api/sessionApi.js';
import { FormError } from '../../shared/ui/FormError.jsx';

const EMPTY_OPTION = { text: '', correct: false, position: 1 };
const EMPTY_QUESTION = {
  text: '',
  imageUrl: '',
  type: 'SINGLE_CHOICE',
  timeLimitSeconds: 30,
  pointsCorrect: 100,
  pointsIncorrect: 0,
  position: 1,
  answerOptions: [
    { ...EMPTY_OPTION, text: 'Правильный ответ', correct: true, position: 1 },
    { ...EMPTY_OPTION, text: 'Неверный ответ', position: 2 },
  ],
};

export function QuizEditorPage() {
  const { quizId } = useParams();
  const navigate = useNavigate();
  const [quiz, setQuiz] = useState(null);
  const [quizForm, setQuizForm] = useState({ title: '', description: '', status: 'DRAFT' });
  const [questionForm, setQuestionForm] = useState(EMPTY_QUESTION);
  const [editingQuestionId, setEditingQuestionId] = useState(null);
  const [error, setError] = useState(null);

  const sortedQuestions = useMemo(
    () => [...(quiz?.questions || [])].sort((a, b) => a.position - b.position),
    [quiz],
  );

  useEffect(() => {
    loadQuiz();
  }, [quizId]);

  async function loadQuiz() {
    const loadedQuiz = await getQuiz(quizId);
    setQuiz(loadedQuiz);
    setQuizForm({
      title: loadedQuiz.title,
      description: loadedQuiz.description || '',
      status: loadedQuiz.status,
    });
    setQuestionForm((current) => ({ ...current, position: loadedQuiz.questions.length + 1 }));
  }

  async function handleQuizSave(event) {
    event.preventDefault();
    setError(null);

    try {
      const updated = await updateQuiz(quizId, quizForm);
      setQuiz(updated);
    } catch (err) {
      setError(err);
    }
  }

  async function handleQuestionSave(event) {
    event.preventDefault();
    setError(null);

    const request = normalizeQuestionRequest(questionForm);

    try {
      if (editingQuestionId) {
        await updateQuestion(quizId, editingQuestionId, request);
      } else {
        await addQuestion(quizId, request);
      }

      setEditingQuestionId(null);
      setQuestionForm({
        ...EMPTY_QUESTION,
        position: sortedQuestions.length + (editingQuestionId ? 0 : 2),
      });
      await loadQuiz();
    } catch (err) {
      setError(err);
    }
  }

  async function handleQuestionDelete(questionId) {
    setError(null);

    try {
      await deleteQuestion(quizId, questionId);
      await loadQuiz();
    } catch (err) {
      setError(err);
    }
  }

  async function handleQuizDelete() {
    setError(null);

    try {
      await deleteQuiz(quizId);
      navigate('/');
    } catch (err) {
      setError(err);
    }
  }

  async function handleStartSession() {
    setError(null);

    try {
      const session = await startSession(quizId);
      navigate(`/sessions/${session.id}/host/${session.roomCode}`);
    } catch (err) {
      setError(err);
    }
  }

  function startEdit(question) {
    setEditingQuestionId(question.id);
    setQuestionForm({
      text: question.text,
      imageUrl: question.imageUrl || '',
      type: question.type,
      timeLimitSeconds: question.timeLimitSeconds,
      pointsCorrect: question.pointsCorrect,
      pointsIncorrect: question.pointsIncorrect,
      position: question.position,
      answerOptions: question.answerOptions.map((option) => ({
        text: option.text,
        correct: option.correct,
        position: option.position,
      })),
    });
  }

  if (!quiz) {
    return <main className="content-grid"><section className="panel">Загружаем квиз...</section></main>;
  }

  return (
    <main className="editor-layout">
      <section className="panel editor-main">
        <div className="panel-heading">
          <div>
            <p className="eyebrow">Квиз</p>
            <h2>{quiz.title}</h2>
          </div>
          <button className="icon-button" type="button" onClick={() => navigate('/')} title="Назад">
            <ArrowLeft size={20} />
          </button>
        </div>

        <form className="form-grid" onSubmit={handleQuizSave}>
          <label>
            Название
            <input value={quizForm.title} onChange={(event) => setQuizForm({ ...quizForm, title: event.target.value })} />
          </label>
          <label>
            Описание
            <textarea
              value={quizForm.description}
              onChange={(event) => setQuizForm({ ...quizForm, description: event.target.value })}
            />
          </label>
          <label>
            Статус
            <select value={quizForm.status} onChange={(event) => setQuizForm({ ...quizForm, status: event.target.value })}>
              <option value="DRAFT">DRAFT</option>
              <option value="PUBLISHED">PUBLISHED</option>
              <option value="ARCHIVED">ARCHIVED</option>
            </select>
          </label>
          <div className="toolbar">
            <button className="button secondary" type="submit">
              <Save size={18} />
              Сохранить
            </button>
            <button className="button primary" type="button" onClick={handleStartSession}>
              <Play size={18} />
              Запустить
            </button>
            <button className="button danger" type="button" onClick={handleQuizDelete}>
              <Trash2 size={18} />
              Удалить
            </button>
          </div>
        </form>

        <FormError error={error} />

        <div className="question-list">
          {sortedQuestions.length === 0 ? (
            <p className="muted">Добавь первый вопрос перед запуском.</p>
          ) : sortedQuestions.map((question) => (
            <article className="question-card" key={question.id}>
              <div>
                <p className="eyebrow">Вопрос {question.position} · {question.type}</p>
                <h3>{question.text}</h3>
                <span className="muted">{question.timeLimitSeconds} сек. · {question.pointsCorrect} оч.</span>
              </div>
              <div className="option-preview">
                {question.answerOptions.map((option) => (
                  <span className={option.correct ? 'correct-option' : ''} key={option.id}>
                    {option.position}. {option.text}
                  </span>
                ))}
              </div>
              <div className="toolbar">
                <button className="button secondary" type="button" onClick={() => startEdit(question)}>Изменить</button>
                <button className="icon-button danger-icon" type="button" onClick={() => handleQuestionDelete(question.id)} title="Удалить">
                  <Trash2 size={18} />
                </button>
              </div>
            </article>
          ))}
        </div>
      </section>

      <section className="panel question-form-panel">
        <div className="panel-heading">
          <div>
            <p className="eyebrow">{editingQuestionId ? 'Редактирование' : 'Новый вопрос'}</p>
            <h2>Вопрос</h2>
          </div>
          <Plus size={22} />
        </div>
        <QuestionForm
          value={questionForm}
          onChange={setQuestionForm}
          onSubmit={handleQuestionSave}
          editing={Boolean(editingQuestionId)}
          onCancel={() => {
            setEditingQuestionId(null);
            setQuestionForm({ ...EMPTY_QUESTION, position: sortedQuestions.length + 1 });
          }}
        />
      </section>
    </main>
  );
}

function QuestionForm({ value, onChange, onSubmit, editing, onCancel }) {
  function updateOption(index, patch) {
    const options = value.answerOptions.map((option, optionIndex) => (
      optionIndex === index ? { ...option, ...patch } : option
    ));
    onChange({ ...value, answerOptions: options });
  }

  function addOption() {
    onChange({
      ...value,
      answerOptions: [
        ...value.answerOptions,
        { ...EMPTY_OPTION, position: value.answerOptions.length + 1 },
      ],
    });
  }

  function removeOption(index) {
    const options = value.answerOptions
      .filter((_, optionIndex) => optionIndex !== index)
      .map((option, optionIndex) => ({ ...option, position: optionIndex + 1 }));
    onChange({ ...value, answerOptions: options });
  }

  return (
    <form className="form-grid" onSubmit={onSubmit}>
      <label>
        Текст
        <textarea value={value.text} onChange={(event) => onChange({ ...value, text: event.target.value })} required />
      </label>
      <label>
        URL изображения
        <input value={value.imageUrl} onChange={(event) => onChange({ ...value, imageUrl: event.target.value })} />
      </label>
      <div className="form-row">
        <label>
          Тип
          <select value={value.type} onChange={(event) => onChange({ ...value, type: event.target.value })}>
            <option value="SINGLE_CHOICE">SINGLE_CHOICE</option>
            <option value="MULTIPLE_CHOICE">MULTIPLE_CHOICE</option>
            <option value="TRUE_FALSE">TRUE_FALSE</option>
          </select>
        </label>
        <label>
          Позиция
          <input type="number" min="1" value={value.position} onChange={(event) => onChange({ ...value, position: Number(event.target.value) })} />
        </label>
      </div>
      <div className="form-row">
        <label>
          Время
          <input type="number" min="1" value={value.timeLimitSeconds} onChange={(event) => onChange({ ...value, timeLimitSeconds: Number(event.target.value) })} />
        </label>
        <label>
          Баллы
          <input type="number" min="0" value={value.pointsCorrect} onChange={(event) => onChange({ ...value, pointsCorrect: Number(event.target.value) })} />
        </label>
      </div>

      <div className="options-editor">
        {value.answerOptions.map((option, index) => (
          <div className="option-editor-row" key={index}>
            <input
              value={option.text}
              onChange={(event) => updateOption(index, { text: event.target.value })}
              required
            />
            <label className="check-label">
              <input
                type="checkbox"
                checked={option.correct}
                onChange={(event) => updateOption(index, { correct: event.target.checked })}
              />
              верный
            </label>
            <button className="icon-button" type="button" onClick={() => removeOption(index)} title="Удалить вариант">
              <Trash2 size={17} />
            </button>
          </div>
        ))}
        <button className="button secondary" type="button" onClick={addOption}>
          <Plus size={18} />
          Вариант
        </button>
      </div>

      <div className="toolbar">
        <button className="button primary" type="submit">
          <Save size={18} />
          {editing ? 'Обновить' : 'Добавить'}
        </button>
        {editing && <button className="button secondary" type="button" onClick={onCancel}>Отмена</button>}
      </div>
    </form>
  );
}

function normalizeQuestionRequest(question) {
  return {
    ...question,
    imageUrl: question.imageUrl || null,
    answerOptions: question.answerOptions.map((option, index) => ({
      ...option,
      position: index + 1,
    })),
  };
}
