import { Navigate, Route, Routes } from 'react-router-dom';
import { AppLayout } from './shared/layout/AppLayout.jsx';
import { LoginPage } from './features/auth/LoginPage.jsx';
import { RegisterPage } from './features/auth/RegisterPage.jsx';
import { DashboardPage } from './features/dashboard/DashboardPage.jsx';
import { QuizEditorPage } from './features/quizzes/QuizEditorPage.jsx';
import { HostSessionPage } from './features/sessions/HostSessionPage.jsx';
import { ParticipantJoinPage } from './features/sessions/ParticipantJoinPage.jsx';
import { ParticipantRoomPage } from './features/sessions/ParticipantRoomPage.jsx';
import { useAuth } from './shared/auth/AuthContext.jsx';

export function App() {
  const { bootstrapping } = useAuth();

  if (bootstrapping) {
    return <div className="app-loading">Quiz Live</div>;
  }

  return (
    <Routes>
      <Route path="/login" element={<PublicRoute><LoginPage /></PublicRoute>} />
      <Route path="/register" element={<PublicRoute><RegisterPage /></PublicRoute>} />
      <Route path="/join" element={<ParticipantJoinPage />} />
      <Route path="/room/:roomCode" element={<ParticipantRoomPage />} />
      <Route path="/" element={<PrivateRoute><AppLayout /></PrivateRoute>}>
        <Route index element={<DashboardPage />} />
        <Route path="quizzes/:quizId" element={<QuizEditorPage />} />
        <Route path="sessions/:sessionId/host/:roomCode" element={<HostSessionPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

function PrivateRoute({ children }) {
  const { isAuthenticated } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return children;
}

function PublicRoute({ children }) {
  const { isAuthenticated } = useAuth();

  if (isAuthenticated) {
    return <Navigate to="/" replace />;
  }

  return children;
}
