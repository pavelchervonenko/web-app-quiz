import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { UserPlus } from 'lucide-react';
import { useAuth } from '../../shared/auth/AuthContext.jsx';
import { FormError } from '../../shared/ui/FormError.jsx';

export function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: '', password: '', displayName: '' });
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setSubmitting(true);
    setError(null);

    try {
      await register(form);
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
          <p className="eyebrow">Новый участник</p>
          <h1>Создай аккаунт и сохраняй историю игр</h1>
          <p>
            После регистрации можно участвовать в квизах и видеть личные результаты в кабинете.
          </p>
        </div>

        <form className="auth-card" onSubmit={handleSubmit}>
          <div>
            <h2>Регистрация</h2>
            <p className="muted">По умолчанию создается аккаунт участника.</p>
          </div>
          <label>
            Имя
            <input
              value={form.displayName}
              onChange={(event) => setForm({ ...form, displayName: event.target.value })}
              required
            />
          </label>
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
              minLength={8}
              value={form.password}
              onChange={(event) => setForm({ ...form, password: event.target.value })}
              required
            />
          </label>
          <FormError error={error} />
          <button className="button primary full" type="submit" disabled={submitting}>
            <UserPlus size={18} />
            {submitting ? 'Создаем...' : 'Создать аккаунт'}
          </button>
          <p className="auth-link">
            Уже есть аккаунт? <Link to="/login">Войти</Link>
          </p>
        </form>
      </section>
    </main>
  );
}
