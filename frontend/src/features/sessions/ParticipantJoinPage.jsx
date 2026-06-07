import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { LogIn, RadioTower } from 'lucide-react';
import { joinSession } from '../../shared/api/sessionApi.js';
import { FormError } from '../../shared/ui/FormError.jsx';

export function ParticipantJoinPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ roomCode: '', displayName: '' });
  const [error, setError] = useState(null);

  async function handleSubmit(event) {
    event.preventDefault();
    setError(null);

    try {
      const response = await joinSession({
        roomCode: form.roomCode,
        displayName: form.displayName,
      });

      sessionStorage.setItem(`participant:${response.roomCode}`, response.participantSessionId);
      navigate(`/room/${response.roomCode}`);
    } catch (err) {
      setError(err);
    }
  }

  return (
    <main className="join-page">
      <section className="join-panel">
        <div className="brand-mark large">
          <RadioTower size={34} />
        </div>
        <h1>Войти в квиз</h1>
        <form className="auth-card compact" onSubmit={handleSubmit}>
          <label>
            Код комнаты
            <input
              value={form.roomCode}
              onChange={(event) => setForm({ ...form, roomCode: event.target.value.toUpperCase() })}
              required
            />
          </label>
          <label>
            Имя на экране
            <input
              value={form.displayName}
              onChange={(event) => setForm({ ...form, displayName: event.target.value })}
              required
            />
          </label>
          <FormError error={error} />
          <button className="button primary full" type="submit">
            <LogIn size={18} />
            Присоединиться
          </button>
        </form>
      </section>
    </main>
  );
}
