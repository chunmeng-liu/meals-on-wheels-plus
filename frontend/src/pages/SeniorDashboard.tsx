import { useEffect, useMemo, useState } from 'react';
import api from '../api/client';
import { useNavigate } from 'react-router-dom';
import type { CompanionRequest, MealRequest, NotificationRecord } from '../api/types';

const tomorrow = () => { const date = new Date(); date.setDate(date.getDate() + 1); return date.toISOString().slice(0, 10); };
const canCancelMeal = (status: string) => ['REQUESTED', 'APPROVED', 'ASSIGNED'].includes(status);
const canCancelCompanion = (status: string) => ['REQUESTED', 'APPROVED', 'SCHEDULED', 'ASSIGNED'].includes(status);
const pretty = (value: string) => value.replace(/_/g, ' ').toLowerCase().replace(/^./, (c: string) => c.toUpperCase());

export default function SeniorDashboard({ onLogout }: { onLogout: () => void }) {
  const navigate = useNavigate();
  const [meals, setMeals] = useState<MealRequest[]>([]);
  const [companions, setCompanions] = useState<CompanionRequest[]>([]);
  const [notifications, setNotifications] = useState<NotificationRecord[]>([]);
  const [mealForm, setMealForm] = useState({ mealType: 'Heart-healthy', quantity: 1, requestedDeliveryDate: tomorrow(), deliveryAddress: '', dietaryNotes: '' });
  const [companionForm, setCompanionForm] = useState({ reason: '', serviceNotes: '', requestedDate: tomorrow(), requestedTime: '10:30' });
  const [message, setMessage] = useState(''); const [error, setError] = useState(''); const [loading, setLoading] = useState(true);

  const upcoming = useMemo(() => companions.filter((item) => ['SCHEDULED', 'ASSIGNED'].includes(item.status) && item.scheduledAt), [companions]);
  const loadData = async () => {
    try {
      const [mealData, companionData, notices] = await Promise.all([api.get<MealRequest[]>('/api/meal-requests/my'), api.get<CompanionRequest[]>('/api/companion-requests/my'), api.get<NotificationRecord[]>('/api/notifications')]);
      setMeals(mealData); setCompanions(companionData); setNotifications(notices); setError('');
    } catch (e) { setError(e instanceof Error ? e.message : 'Unable to load dashboard'); }
    finally { setLoading(false); }
  };
  useEffect(() => { loadData(); }, []);

  const submitMeal = async (e: React.FormEvent) => { e.preventDefault(); setError(''); try { const data = await api.post<MealRequest>('/api/meal-requests', { ...mealForm, quantity: Number(mealForm.quantity) }); setMessage(`Meal request #${data.id} submitted`); setMealForm({ ...mealForm, dietaryNotes: '' }); loadData(); } catch (err) { setError(err instanceof Error ? err.message : 'Unable to submit meal'); } };
  const submitCompanion = async (e: React.FormEvent) => { e.preventDefault(); setError(''); try { const data = await api.post<CompanionRequest>('/api/companion-requests', companionForm); setMessage(`Companion request #${data.id} submitted`); setCompanionForm({ ...companionForm, reason: '', serviceNotes: '' }); loadData(); } catch (err) { setError(err instanceof Error ? err.message : 'Unable to submit service'); } };
  const cancel = async (kind: 'meal' | 'companion', id: number) => { if (!window.confirm('Cancel this request?')) return; try { await api.del(`/api/${kind === 'meal' ? 'meal-requests' : 'companion-requests'}/${id}`); setMessage('Request cancelled'); loadData(); } catch (e) { setError(e instanceof Error ? e.message : 'Unable to cancel'); } };
  const readNotice = async (id: number) => { await api.put(`/api/notifications/${id}/read`, {}); loadData(); };

  return <main className="page wide">
    <header className="topbar"><div><span className="eyebrow">Senior portal</span><h1>Your services</h1></div><div className="button-row"><button className="button secondary" onClick={() => navigate('/profile')}>Profile</button><button className="button ghost" onClick={onLogout}>Logout</button></div></header>
    {message && <p className="notice success">{message}</p>}{error && <p className="notice error" role="alert">{error}</p>}
    {loading ? <div className="card">Loading your services…</div> : <>
      <section className="stat-grid"><article className="stat"><strong>{meals.filter((x) => !['DELIVERED','CANCELLED','REJECTED'].includes(x.status)).length}</strong><span>Active meals</span></article><article className="stat"><strong>{companions.filter((x) => !['COMPLETED','CANCELLED','REJECTED'].includes(x.status)).length}</strong><span>Active visits</span></article><article className="stat"><strong>{notifications.filter((x) => !x.read).length}</strong><span>Unread updates</span></article></section>
      {upcoming.length > 0 && <section className="card accent"><h2>Upcoming companion services</h2>{upcoming.map((item) => <p key={item.id}><strong>{new Date(item.scheduledAt!).toLocaleString()}</strong> · {item.reason}{item.assignedVolunteerName ? ` · ${item.assignedVolunteerName}` : ''}</p>)}</section>}
      <div className="two-column">
        <form className="card form-grid" onSubmit={submitMeal}><div className="full section-heading"><span>Meal delivery</span><h2>Request a meal</h2></div><label>Delivery date<input type="date" min={tomorrow()} required value={mealForm.requestedDeliveryDate} onChange={(e) => setMealForm({ ...mealForm, requestedDeliveryDate: e.target.value })} /></label><label>Meal preference<input required value={mealForm.mealType} onChange={(e) => setMealForm({ ...mealForm, mealType: e.target.value })} /></label><label>Quantity<input type="number" min="1" max="20" required value={mealForm.quantity} onChange={(e) => setMealForm({ ...mealForm, quantity: Number(e.target.value) })} /></label><label>Delivery address<input required value={mealForm.deliveryAddress} onChange={(e) => setMealForm({ ...mealForm, deliveryAddress: e.target.value })} /></label><label className="full">Dietary notes<textarea value={mealForm.dietaryNotes} onChange={(e) => setMealForm({ ...mealForm, dietaryNotes: e.target.value })} /></label><button className="button full" type="submit">Submit meal request</button></form>
        <form className="card form-grid" onSubmit={submitCompanion}><div className="full section-heading"><span>Companion service</span><h2>Request a visit</h2></div><label>Date<input type="date" min={tomorrow()} required value={companionForm.requestedDate} onChange={(e) => setCompanionForm({ ...companionForm, requestedDate: e.target.value })} /></label><label>Preferred time<input type="time" required value={companionForm.requestedTime} onChange={(e) => setCompanionForm({ ...companionForm, requestedTime: e.target.value })} /></label><label className="full">How can we help?<input required value={companionForm.reason} onChange={(e) => setCompanionForm({ ...companionForm, reason: e.target.value })} /></label><label className="full">Service notes<textarea value={companionForm.serviceNotes} onChange={(e) => setCompanionForm({ ...companionForm, serviceNotes: e.target.value })} /></label><button className="button full" type="submit">Submit companion request</button></form>
      </div>
      <section className="card"><div className="section-heading"><span>Tracking</span><h2>Meal request history</h2></div>{meals.length === 0 ? <p className="muted">No meal requests yet.</p> : <div className="table-wrap"><table><thead><tr><th>Date</th><th>Meal</th><th>Status</th><th>Volunteer</th><th>Notes</th><th></th></tr></thead><tbody>{meals.map((meal) => <tr key={meal.id}><td>{meal.requestedDeliveryDate}</td><td>{meal.mealType} × {meal.quantity}</td><td><span className={`badge ${meal.status.toLowerCase()}`}>{pretty(meal.status)}</span></td><td>{meal.assignedVolunteerName || '—'}</td><td>{meal.completionNotes || meal.adminNotes || '—'}</td><td>{canCancelMeal(meal.status) && <button className="text-button danger" onClick={() => cancel('meal', meal.id)}>Cancel</button>}</td></tr>)}</tbody></table></div>}</section>
      <section className="card"><div className="section-heading"><span>Tracking</span><h2>Companion request history</h2></div>{companions.length === 0 ? <p className="muted">No companion requests yet.</p> : <div className="table-wrap"><table><thead><tr><th>Requested</th><th>Reason</th><th>Status</th><th>Scheduled</th><th>Notes</th><th></th></tr></thead><tbody>{companions.map((item) => <tr key={item.id}><td>{item.requestedDate} {item.requestedTime}</td><td>{item.reason}</td><td><span className={`badge ${item.status.toLowerCase()}`}>{pretty(item.status)}</span></td><td>{item.scheduledAt ? new Date(item.scheduledAt).toLocaleString() : '—'}</td><td>{item.completionNotes || item.adminNotes || '—'}</td><td>{canCancelCompanion(item.status) && <button className="text-button danger" onClick={() => cancel('companion', item.id)}>Cancel</button>}</td></tr>)}</tbody></table></div>}</section>
      <section className="card"><div className="section-heading"><span>Updates</span><h2>Notifications</h2></div>{notifications.slice(0, 8).map((note) => <button key={note.id} className={`notification ${note.read ? 'read' : ''}`} onClick={() => !note.read && readNotice(note.id)}><span><strong>{note.title}</strong><small>{note.message}</small></span><time>{new Date(note.createdAt).toLocaleDateString()}</time></button>)}</section>
    </>}
  </main>;
}
