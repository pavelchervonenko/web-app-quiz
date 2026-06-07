import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { LogIn } from 'lucide-react';
import { useAuth } from '../../shared/auth/AuthContext.jsx';
import { FormError } from '../../shared/ui/FormError.jsx';

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: 'organizer@example.com', password: 'password123' });
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setSubmitting(true);
    setError(null);

    try {
      await login(form);
      navigate('/');
    } catch (err) {
      setError(err);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <main className="auth-page">
      <section className="auth-panel">
        <div className="auth-copy">
          <p className="eyebrow">Quiz Live</p>
          <h1>Управляй квизом в реальном времени</h1>
          <p>
            Создавай вопросы, запускай комнату и смотри, как участники двигаются по лидерборду.
          </p>
        </div>

        <form className="auth-card" onSubmit={handleSubmit}>
          <div>
            <h2>Вход</h2>
            <p className="muted">Войди как организатор или участник.</p>
          </div>
          <label>
            Email
            <input
              type="email"
              value={form.email}
              onChange={(event) => setForm({ ...form, email: event.target.value })}
              required
            />
          </label>
          <label>
            Пароль
            <input
              type="password"
              value={form.password}
              onChange={(event) => setForm({ ...form, password: event.target.value })}
              required
            />
          </label>
          <FormError error={error} />
          <button className="button primary full" type="submit" disabled={submitting}>
            <LogIn size={18} />
            {submitting ? 'Входим...' : 'Войти'}
          </button>
          <p className="auth-link">
            Нет аккаунта? <Link to="/register">Создать</Link>
          </p>
        </form>
      </section>
    </main>
  );
}
