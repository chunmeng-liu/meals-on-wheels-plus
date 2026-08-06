import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { login } from '../api/auth';

const demos = [
  ['Admin', 'admin@mealsplus.local', 'Admin123!'],
  ['Senior', 'senior@mealsplus.local', 'Senior123!'],
  ['Volunteer', 'volunteer@mealsplus.local', 'Volunteer123!']
];

export default function LoginPage({ onAuth }: { onAuth: (user: any) => void }) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault(); setError(''); setSubmitting(true);
    try {
      const res = await login(email, password);
      onAuth({ id: 0, email: res.email, firstName: res.firstName, lastName: res.lastName, role: res.role, active: true });
      navigate(`/${res.role.toLowerCase()}`);
    } catch (err) { setError(err instanceof Error ? err.message : 'Login failed'); }
    finally { setSubmitting(false); }
  };

  return (
    <main className="auth-page">
      <form className="card auth-card" onSubmit={handleSubmit}>
        <Link className="back-link" to="/">← Home</Link>
        <span className="eyebrow">Welcome back</span><h1>Sign in</h1>
        <label>Email<input type="email" required autoComplete="username" value={email} onChange={(e) => setEmail(e.target.value)} /></label>
        <label>Password<input type="password" required autoComplete="current-password" value={password} onChange={(e) => setPassword(e.target.value)} /></label>
        {error && <p className="notice error" role="alert">{error}</p>}
        <button className="button" type="submit" disabled={submitting}>{submitting ? 'Signing in…' : 'Sign in'}</button>
        <div className="demo-box"><strong>Local demo accounts</strong><p>Choose a role to fill the form.</p>
          <div className="button-row">{demos.map(([label, demoEmail, demoPassword]) => <button key={label} type="button" className="button ghost small" onClick={() => { setEmail(demoEmail); setPassword(demoPassword); }}>{label}</button>)}</div>
        </div>
      </form>
    </main>
  );
}
