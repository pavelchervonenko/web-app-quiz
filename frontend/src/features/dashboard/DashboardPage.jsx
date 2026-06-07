import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { History, Play, Plus, RadioTower, Save, UserRound } from 'lucide-react';
import { useAuth } from '../../shared/auth/AuthContext.jsx';
import { createQuiz, getMyQuizzes } from '../../shared/api/quizApi.js';
import { getOrganizedSessions, getParticipations, updateMe } from '../../shared/api/userApi.js';
import { FormError } from '../../shared/ui/FormError.jsx';

export function DashboardPage() {
  const { user, updateUser } = useAuth();
  const navigate = useNavigate();
  const [quizzes, setQuizzes] = useState([]);
  const [organizedSessions, setOrganizedSessions] = useState([]);
  const [participations, setParticipations] = useState([]);
  const [profile, setProfile] = useState({ email: user?.email || '', displayName: user?.displayName || '' });
  const [newQuiz, setNewQuiz] = useState({ title: '', description: '' });
  const [error, setError] = useState(null);

  useEffect(() => {
    setProfile({ email: user?.email || '', displayName: user?.displayName || '' });
  }, [user]);

  useEffect(() => {
    refresh();
  }, []);

  async function refresh() {
    const requests = [
      getParticipations().then(setParticipations),
    ];

    if (user?.role !== 'PARTICIPANT') {
      requests.push(getMyQuizzes().then(setQuizzes));
      requests.push(getOrganizedSessions().then(setOrganizedSessions));
    }

    await Promise.all(requests);
  }

  async function handleCreateQuiz(event) {
    event.preventDefault();
    setError(null);

    try {
      const quiz = await createQuiz(newQuiz);
      setNewQuiz({ title: '', description: '' });
      navigate(`/quizzes/${quiz.id}`);
    } catch (err) {
      setError(err);
    }
  }

  async function handleProfileSave(event) {
    event.preventDefault();
    setError(null);

    try {
      const updatedUser = await updateMe(profile);
      updateUser(updatedUser);
    } catch (err) {
      setError(err);
    }
  }

  return (
    <main className="content-grid">
      <section className="panel profile-panel">
        <div className="panel-heading">
          <div>
            <p className="eyebrow">Профиль</p>
            <h2>{user?.displayName}</h2>
          </div>
          <UserRound size={22} />
        </div>
        <form className="form-grid" onSubmit={handleProfileSave}>
          <label>
            Имя
            <input value={profile.displayName} onChange={(event) => setProfile({ ...profile, displayName: event.target.value })} />
          </label>
          <label>
            Email
            <input type="email" value={profile.email} onChange={(event) => setProfile({ ...profile, email: event.target.value })} />
          </label>
          <button className="button secondary" type="submit">
            <Save size={18} />
            Сохранить
          </button>
        </form>
        <FormError error={error} />
      </section>

      <section className="panel action-panel">
        <div className="panel-heading">
          <div>
            <p className="eyebrow">Участник</p>
            <h2>Войти в комнату</h2>
          </div>
          <RadioTower size={22} />
        </div>
        <button className="button primary full" type="button" onClick={() => navigate('/join')}>
          <Play size={18} />
          Присоединиться
        </button>
      </section>

      {user?.role !== 'PARTICIPANT' && (
        <>
          <section className="panel create-panel">
            <div className="panel-heading">
              <div>
                <p className="eyebrow">Организатор</p>
                <h2>Новый квиз</h2>
              </div>
              <Plus size={22} />
            </div>
            <form className="form-grid" onSubmit={handleCreateQuiz}>
              <label>
                Название
                <input
                  value={newQuiz.title}
                  onChange={(event) => setNewQuiz({ ...newQuiz, title: event.target.value })}
                  required
                />
              </label>
              <label>
                Описание
                <textarea
                  value={newQuiz.description}
                  onChange={(event) => setNewQuiz({ ...newQuiz, description: event.target.value })}
                />
              </label>
              <button className="button primary" type="submit">
                <Plus size={18} />
                Создать
              </button>
            </form>
          </section>

          <section className="panel wide">
            <div className="panel-heading">
              <div>
                <p className="eyebrow">Мои квизы</p>
                <h2>Редактор</h2>
              </div>
              <History size={22} />
            </div>
            <div className="card-list">
              {quizzes.length === 0 ? (
                <p className="muted">Квизов пока нет.</p>
              ) : quizzes.map((quiz) => (
                <Link className="item-card" key={quiz.id} to={`/quizzes/${quiz.id}`}>
                  <div>
                    <strong>{quiz.title}</strong>
                    <span>{quiz.description || 'Без описания'}</span>
                  </div>
                  <div className="item-meta">
                    <span>{quiz.status}</span>
                    <span>{quiz.questionCount} вопр.</span>
                  </div>
                </Link>
              ))}
            </div>
          </section>

          <HistoryPanel title="Проведенные сессии" items={organizedSessions} kind="organized" />
        </>
      )}

      <HistoryPanel title="История участия" items={participations} kind="participation" />
    </main>
  );
}

function HistoryPanel({ title, items, kind }) {
  return (
    <section className="panel wide">
      <div className="panel-heading">
        <div>
          <p className="eyebrow">История</p>
          <h2>{title}</h2>
        </div>
        <History size={22} />
      </div>
      <div className="table-list">
        {items.length === 0 ? (
          <p className="muted">Записей пока нет.</p>
        ) : items.map((item) => (
          <div className="table-row" key={kind === 'organized' ? item.sessionId : item.participantSessionId}>
            <div>
              <strong>{item.quizTitle}</strong>
              <span>{item.roomCode} · {item.status}</span>
            </div>
            <strong>{kind === 'organized' ? `${item.participantCount} уч.` : `${item.score} оч.`}</strong>
          </div>
        ))}
      </div>
    </section>
  );
}
