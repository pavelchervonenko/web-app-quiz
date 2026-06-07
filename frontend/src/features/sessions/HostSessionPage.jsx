import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { ArrowLeft, CheckCircle2, Copy, FastForward, Flag } from 'lucide-react';
import { closeCurrentQuestion, finishSession, getSessionState, showNextQuestion } from '../../shared/api/sessionApi.js';
import { createSessionStateClient } from '../../shared/realtime/sessionRealtime.js';
import { Leaderboard } from '../../shared/ui/Leaderboard.jsx';
import { FormError } from '../../shared/ui/FormError.jsx';

export function HostSessionPage() {
  const { sessionId, roomCode } = useParams();
  const navigate = useNavigate();
  const [state, setState] = useState(null);
  const [connection, setConnection] = useState('connecting');
  const [error, setError] = useState(null);

  useEffect(() => {
    getSessionState(roomCode).then(setState).catch(setError);

    const client = createSessionStateClient(roomCode, setState, setConnection);
    client.activate();

    return () => client.deactivate();
  }, [roomCode]);

  async function runAction(action) {
    setError(null);

    try {
      const nextState = await action(sessionId);
      setState(nextState);
    } catch (err) {
      setError(err);
    }
  }

  return (
    <main className="session-layout">
      <section className="panel session-hero">
        <div className="session-hero-top">
          <button className="icon-button" type="button" onClick={() => navigate('/')} title="Назад">
            <ArrowLeft size={20} />
          </button>
          <span className={`connection ${connection}`}>{connection}</span>
        </div>
        <p className="eyebrow">Код комнаты</p>
        <div className="room-code">
          <span>{roomCode}</span>
          <button className="icon-button" type="button" onClick={() => navigator.clipboard.writeText(roomCode)} title="Скопировать">
            <Copy size={20} />
          </button>
        </div>
        <p className="muted">Участники подключаются по этому коду.</p>
        <div className="toolbar">
          <button className="button primary" type="button" onClick={() => runAction(showNextQuestion)}>
            <FastForward size={18} />
            Следующий вопрос
          </button>
          <button className="button secondary" type="button" onClick={() => runAction(closeCurrentQuestion)}>
            <CheckCircle2 size={18} />
            Закрыть вопрос
          </button>
          <button className="button danger" type="button" onClick={() => runAction(finishSession)}>
            <Flag size={18} />
            Завершить
          </button>
        </div>
        <FormError error={error} />
      </section>

      <section className="panel current-question-panel">
        <p className="eyebrow">Состояние: {state?.status || '...'}</p>
        {state?.currentQuestion ? (
          <>
            <h2>{state.currentQuestion.text}</h2>
            {state.currentQuestion.imageUrl && <img className="question-image" src={state.currentQuestion.imageUrl} alt="" />}
            <div className="answer-grid readonly">
              {state.currentQuestion.options.map((option) => (
                <div className="answer-option" key={option.id}>
                  {option.position}. {option.text}
                </div>
              ))}
            </div>
          </>
        ) : (
          <h2>Ожидаем запуск вопроса</h2>
        )}
      </section>

      <Leaderboard leaderboard={state?.leaderboard} />
    </main>
  );
}
