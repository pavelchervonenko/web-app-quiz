import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { ArrowLeft, CheckCircle2, Send } from 'lucide-react';
import { getSessionState, submitAnswer } from '../../shared/api/sessionApi.js';
import { createSessionStateClient } from '../../shared/realtime/sessionRealtime.js';
import { Leaderboard } from '../../shared/ui/Leaderboard.jsx';
import { FormError } from '../../shared/ui/FormError.jsx';

export function ParticipantRoomPage() {
  const { roomCode } = useParams();
  const navigate = useNavigate();
  const [state, setState] = useState(null);
  const [connection, setConnection] = useState('connecting');
  const [selectedOptions, setSelectedOptions] = useState([]);
  const [answerResult, setAnswerResult] = useState(null);
  const [error, setError] = useState(null);

  const participantSessionId = sessionStorage.getItem(`participant:${roomCode}`);
  const currentQuestion = state?.currentQuestion;
  const canAnswer = state?.status === 'QUESTION_ACTIVE' && currentQuestion && participantSessionId;

  const selectedSet = useMemo(() => new Set(selectedOptions), [selectedOptions]);

  useEffect(() => {
    getSessionState(roomCode).then(setState).catch(setError);

    const client = createSessionStateClient(roomCode, (nextState) => {
      setState(nextState);
      setSelectedOptions([]);
      setAnswerResult(null);
    }, setConnection);
    client.activate();

    return () => client.deactivate();
  }, [roomCode]);

  function toggleOption(optionId) {
    if (currentQuestion.type === 'MULTIPLE_CHOICE') {
      setSelectedOptions((current) => (
        current.includes(optionId)
          ? current.filter((id) => id !== optionId)
          : [...current, optionId]
      ));
      return;
    }

    setSelectedOptions([optionId]);
  }

  async function handleSubmit() {
    setError(null);

    try {
      const result = await submitAnswer(state.quizSessionId, {
        participantSessionId,
        questionId: currentQuestion.id,
        selectedAnswerOptionIds: selectedOptions,
      });
      setAnswerResult(result);
    } catch (err) {
      setError(err);
    }
  }

  if (!participantSessionId) {
    return (
      <main className="join-page">
        <section className="panel missing-participant">
          <h1>Нужно войти в комнату</h1>
          <button className="button primary" type="button" onClick={() => navigate('/join')}>
            <ArrowLeft size={18} />
            К форме входа
          </button>
        </section>
      </main>
    );
  }

  return (
    <main className="participant-room">
      <section className="panel participant-question">
        <div className="session-hero-top">
          <button className="icon-button" type="button" onClick={() => navigate('/join')} title="Выйти">
            <ArrowLeft size={20} />
          </button>
          <span className={`connection ${connection}`}>{connection}</span>
        </div>
        <p className="eyebrow">{roomCode} · {state?.status || '...'}</p>
        {currentQuestion ? (
          <>
            <h1>{currentQuestion.text}</h1>
            {currentQuestion.imageUrl && <img className="question-image" src={currentQuestion.imageUrl} alt="" />}
            <div className="answer-grid">
              {currentQuestion.options.map((option) => (
                <button
                  className={`answer-option ${selectedSet.has(option.id) ? 'selected' : ''}`}
                  type="button"
                  key={option.id}
                  disabled={!canAnswer || Boolean(answerResult)}
                  onClick={() => toggleOption(option.id)}
                >
                  {option.text}
                </button>
              ))}
            </div>
            <div className="toolbar">
              <button
                className="button primary"
                type="button"
                disabled={!canAnswer || selectedOptions.length === 0 || Boolean(answerResult)}
                onClick={handleSubmit}
              >
                <Send size={18} />
                Ответить
              </button>
              {answerResult && (
                <span className={`answer-result ${answerResult.correct ? 'success' : 'neutral'}`}>
                  <CheckCircle2 size={18} />
                  {answerResult.correct ? 'Верно' : 'Ответ принят'} · {answerResult.totalScore} оч.
                </span>
              )}
            </div>
          </>
        ) : (
          <h1>Ожидаем вопрос</h1>
        )}
        <FormError error={error} />
      </section>

      <Leaderboard leaderboard={state?.leaderboard} />
    </main>
  );
}
