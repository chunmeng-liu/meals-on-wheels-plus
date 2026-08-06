import { useEffect, useState } from 'react';
import api from '../api/client';
import { useNavigate } from 'react-router-dom';
import type { CompanionRequest, MealRequest, NotificationRecord } from '../api/types';

const mealNext: Record<string, string> = { ASSIGNED: 'PREPARING', PREPARING: 'OUT_FOR_DELIVERY', OUT_FOR_DELIVERY: 'DELIVERED' };
const companionNext: Record<string, string> = { ASSIGNED: 'IN_PROGRESS', IN_PROGRESS: 'COMPLETED' };
const pretty = (value: string) => value.replace(/_/g, ' ').toLowerCase().replace(/^./, (c: string) => c.toUpperCase());

export default function VolunteerDashboard({ onLogout }: { onLogout: () => void }) {
  const navigate = useNavigate();
  const [meals, setMeals] = useState<MealRequest[]>([]); const [companions, setCompanions] = useState<CompanionRequest[]>([]); const [notifications, setNotifications] = useState<NotificationRecord[]>([]);
  const [notes, setNotes] = useState<Record<string, string>>({}); const [message, setMessage] = useState(''); const [error, setError] = useState(''); const [loading, setLoading] = useState(true);

  const loadData = async () => { try { const [mealData, companionData, noticeData] = await Promise.all([api.get<MealRequest[]>('/api/meal-requests/assigned'), api.get<CompanionRequest[]>('/api/companion-requests/assigned'), api.get<NotificationRecord[]>('/api/notifications')]); setMeals(mealData); setCompanions(companionData); setNotifications(noticeData); setError(''); } catch (e) { setError(e instanceof Error ? e.message : 'Unable to load assignments'); } finally { setLoading(false); } };
  useEffect(() => { loadData(); }, []);
  const update = async (kind: 'meal' | 'companion', id: number, status: string) => { try { const key = `${kind}-${id}`; await api.put(`/api/${kind === 'meal' ? 'meal-requests' : 'companion-requests'}/${id}/status`, { status, completionNotes: notes[key] || undefined }); setMessage(`Assignment updated to ${pretty(status)}`); loadData(); } catch (e) { setError(e instanceof Error ? e.message : 'Unable to update assignment'); } };

  return <main className="page wide">
    <header className="topbar"><div><span className="eyebrow">Volunteer portal</span><h1>Your assignments</h1></div><div className="button-row"><button className="button secondary" onClick={() => navigate('/profile')}>Availability</button><button className="button ghost" onClick={onLogout}>Logout</button></div></header>
    {message && <p className="notice success">{message}</p>}{error && <p className="notice error" role="alert">{error}</p>}
    {loading ? <div className="card">Loading assignments…</div> : <>
      <section className="stat-grid"><article className="stat"><strong>{meals.filter((x) => x.status !== 'DELIVERED').length}</strong><span>Meal deliveries</span></article><article className="stat"><strong>{companions.filter((x) => x.status !== 'COMPLETED').length}</strong><span>Companion visits</span></article><article className="stat"><strong>{notifications.filter((x) => !x.read).length}</strong><span>Unread updates</span></article></section>
      <section><div className="section-heading"><span>Meals</span><h2>Assigned deliveries</h2></div>{meals.length === 0 ? <div className="card empty">No meal deliveries are assigned to you.</div> : <div className="assignment-grid">{meals.map((meal) => <article className="card assignment" key={meal.id}><div className="assignment-head"><span className={`badge ${meal.status.toLowerCase()}`}>{pretty(meal.status)}</span><time>{meal.requestedDeliveryDate}</time></div><h3>{meal.mealType} × {meal.quantity}</h3><p><strong>{meal.seniorName}</strong>{meal.seniorPhone ? ` · ${meal.seniorPhone}` : ''}</p><p>{meal.deliveryAddress}</p>{meal.dietaryNotes && <p className="callout">Dietary note: {meal.dietaryNotes}</p>}{mealNext[meal.status] && <><label>Completion / handoff note<textarea value={notes[`meal-${meal.id}`] || ''} onChange={(e) => setNotes({ ...notes, [`meal-${meal.id}`]: e.target.value })} /></label><button className="button" onClick={() => update('meal', meal.id, mealNext[meal.status])}>Mark {pretty(mealNext[meal.status])}</button></>}</article>)}</div>}</section>
      <section><div className="section-heading"><span>Companion care</span><h2>Assigned services</h2></div>{companions.length === 0 ? <div className="card empty">No companion services are assigned to you.</div> : <div className="assignment-grid">{companions.map((item) => <article className="card assignment" key={item.id}><div className="assignment-head"><span className={`badge ${item.status.toLowerCase()}`}>{pretty(item.status)}</span><time>{item.scheduledAt ? new Date(item.scheduledAt).toLocaleString() : `${item.requestedDate} ${item.requestedTime}`}</time></div><h3>{item.reason}</h3><p><strong>{item.seniorName}</strong>{item.seniorPhone ? ` · ${item.seniorPhone}` : ''}</p><p>{item.seniorAddress || 'Address available from coordinator'}</p>{item.serviceNotes && <p className="callout">Service note: {item.serviceNotes}</p>}{companionNext[item.status] && <><label>Completion / handoff note<textarea value={notes[`companion-${item.id}`] || ''} onChange={(e) => setNotes({ ...notes, [`companion-${item.id}`]: e.target.value })} /></label><button className="button" onClick={() => update('companion', item.id, companionNext[item.status])}>Mark {pretty(companionNext[item.status])}</button></>}</article>)}</div>}</section>
    </>}
  </main>;
}
