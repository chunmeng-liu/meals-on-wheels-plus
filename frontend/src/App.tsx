import { Navigate, Route, Routes } from 'react-router-dom';
import { useEffect, useState } from 'react';
import LoginPage from './pages/LoginPage';
import LandingPage from './pages/LandingPage';
import SeniorDashboard from './pages/SeniorDashboard';
import VolunteerDashboard from './pages/VolunteerDashboard';
import AdminDashboard from './pages/AdminDashboard';
import ProfilePage from './pages/ProfilePage';
import { getStoredToken, getStoredUser, setStoredUser, clearAuth } from './api/auth';

function App() {
  const [user, setUser] = useState(getStoredUser());
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = getStoredToken();
    if (!token) {
      setLoading(false);
      return;
    }
    fetch('/api/auth/me', { headers: { Authorization: `Bearer ${token}` } })
      .then((res) => {
        if (!res.ok) throw new Error('Session invalid');
        return res.json();
      })
      .then((data) => {
        setStoredUser(data);
        setUser(data);
      })
      .catch(() => {
        clearAuth();
      })
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="page">Loading...</div>;

  return (
    <Routes>
      <Route path="/" element={user ? <Navigate to={`/${user.role.toLowerCase()}`} /> : <LandingPage />} />
      <Route path="/login" element={user ? <Navigate to={`/${user.role.toLowerCase()}`} /> : <LoginPage onAuth={setUser} />} />
      <Route path="/senior" element={user?.role === 'SENIOR' ? <SeniorDashboard onLogout={() => { clearAuth(); setUser(null); }} /> : <Navigate to="/login" />} />
      <Route path="/volunteer" element={user?.role === 'VOLUNTEER' ? <VolunteerDashboard onLogout={() => { clearAuth(); setUser(null); }} /> : <Navigate to="/login" />} />
      <Route path="/admin" element={user?.role === 'ADMIN' ? <AdminDashboard onLogout={() => { clearAuth(); setUser(null); }} /> : <Navigate to="/login" />} />
      <Route path="/profile" element={user ? <ProfilePage onLogout={() => { clearAuth(); setUser(null); }} /> : <Navigate to="/login" />} />
      <Route path="*" element={<Navigate to="/" />} />
    </Routes>
  );
}

export default App;
