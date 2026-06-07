import { LogOut, Plus, RadioTower, UserRound } from 'lucide-react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext.jsx';

export function AppLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate('/login');
  }

  return (
    <div className="shell">
      <aside className="sidebar">
        <div className="brand-mark">
          <span>QL</span>
        </div>
        <nav className="sidebar-nav">
          <NavLink to="/" end title="Кабинет">
            <UserRound size={21} />
          </NavLink>
          <NavLink to="/join" title="Присоединиться">
            <RadioTower size={21} />
          </NavLink>
        </nav>
      </aside>

      <div className="main">
        <header className="topbar">
          <div>
            <p className="eyebrow">Quiz Live</p>
            <h1>Интерактивные квизы</h1>
          </div>
          <div className="topbar-actions">
            <button className="button secondary" type="button" onClick={() => navigate('/join')}>
              <RadioTower size={18} />
              Комната
            </button>
            {user?.role !== 'PARTICIPANT' && (
              <button className="button primary" type="button" onClick={() => navigate('/')}>
                <Plus size={18} />
                Квиз
              </button>
            )}
            <button className="icon-button" type="button" onClick={handleLogout} title="Выйти">
              <LogOut size={20} />
            </button>
          </div>
        </header>
        <Outlet />
      </div>
    </div>
  );
}
