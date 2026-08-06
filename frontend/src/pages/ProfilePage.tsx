import { useEffect, useState } from 'react';
import api from '../api/client';
import { useNavigate } from 'react-router-dom';

interface Profile { id: number; email: string; firstName: string; lastName: string; phone?: string; role: string; seniorProfile?: Record<string, string>; volunteerProfile?: Record<string, string>; }

export default function ProfilePage({ onLogout }: { onLogout: () => void }) {
  const navigate = useNavigate();
  const [profile, setProfile] = useState<Profile | null>(null);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  const load = () => api.get<Profile>('/api/profile').then(setProfile).catch((e: Error) => setError(e.message));
  useEffect(() => { void load(); }, []);

  const update = (section: 'basic' | 'seniorProfile' | 'volunteerProfile', key: string, value: string) => {
    setProfile((current) => current ? section === 'basic' ? { ...current, [key]: value } : { ...current, [section]: { ...current[section], [key]: value } } : current);
  };
  const save = async (e: React.FormEvent) => {
    e.preventDefault(); if (!profile) return; setError('');
    try {
      await api.put('/api/profile', { firstName: profile.firstName, lastName: profile.lastName, phone: profile.phone || '' });
      if (profile.role === 'SENIOR') await api.put('/api/profile/senior', profile.seniorProfile || {});
      if (profile.role === 'VOLUNTEER') await api.put('/api/profile/volunteer', profile.volunteerProfile || {});
      setMessage('Profile saved'); load();
    } catch (e) { setError(e instanceof Error ? e.message : 'Unable to save profile'); }
  };

  return <main className="page">
    <header className="topbar"><div><span className="eyebrow">Account</span><h1>Your profile</h1></div><div className="button-row"><button className="button secondary" onClick={() => navigate(-1)}>Back</button><button className="button ghost" onClick={onLogout}>Logout</button></div></header>
    {message && <p className="notice success">{message}</p>}{error && <p className="notice error" role="alert">{error}</p>}
    {!profile ? <div className="card">Loading profile…</div> : <form className="card form-grid" onSubmit={save}>
      <label>First name<input required value={profile.firstName} onChange={(e) => update('basic', 'firstName', e.target.value)} /></label>
      <label>Last name<input required value={profile.lastName} onChange={(e) => update('basic', 'lastName', e.target.value)} /></label>
      <label>Email<input value={profile.email} disabled /></label><label>Phone<input value={profile.phone || ''} onChange={(e) => update('basic', 'phone', e.target.value)} /></label>
      {profile.role === 'SENIOR' && <>
        <label className="full">Delivery address<input value={profile.seniorProfile?.address || ''} onChange={(e) => update('seniorProfile', 'address', e.target.value)} /></label>
        <label>Dietary notes<textarea value={profile.seniorProfile?.dietaryNotes || ''} onChange={(e) => update('seniorProfile', 'dietaryNotes', e.target.value)} /></label>
        <label>Mobility notes<textarea value={profile.seniorProfile?.mobilityNotes || ''} onChange={(e) => update('seniorProfile', 'mobilityNotes', e.target.value)} /></label>
        <label>Emergency contact<input value={profile.seniorProfile?.emergencyContactName || ''} onChange={(e) => update('seniorProfile', 'emergencyContactName', e.target.value)} /></label>
        <label>Emergency phone<input value={profile.seniorProfile?.emergencyContactPhone || ''} onChange={(e) => update('seniorProfile', 'emergencyContactPhone', e.target.value)} /></label>
      </>}
      {profile.role === 'VOLUNTEER' && <label className="full">Availability notes<textarea value={profile.volunteerProfile?.availabilityNotes || ''} onChange={(e) => update('volunteerProfile', 'availabilityNotes', e.target.value)} /></label>}
      <div className="full"><button className="button" type="submit">Save changes</button></div>
    </form>}
  </main>;
}
