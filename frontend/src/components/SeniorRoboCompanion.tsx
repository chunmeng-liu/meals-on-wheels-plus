import { useEffect, useMemo, useState } from 'react';
import api from '../api/client';
import type { RoboCompanionVisit } from '../api/types';

const tomorrow = () => { const d = new Date(); d.setDate(d.getDate() + 1); return d.toISOString().slice(0, 10); };
const pretty = (s: string) => s.replace(/_/g, ' ').toLowerCase().replace(/^./, c => c.toUpperCase());
const cancellable = (s: string) => ['REQUESTED','APPROVED','SCHEDULED','ASSIGNED'].includes(s);

export default function SeniorRoboCompanion() {
  const [visits,setVisits]=useState<RoboCompanionVisit[]>([]); const [error,setError]=useState(''); const [message,setMessage]=useState('');
  const [form,setForm]=useState({requestedDate:tomorrow(),requestedTime:'14:00',reason:'',assistanceNeeds:'',serviceNotes:''});
  const load=async()=>{try{setVisits(await api.get<RoboCompanionVisit[]>('/api/robocompanion-requests/my'));setError('');}catch(e){setError(e instanceof Error?e.message:'Unable to load RoboCompanion visits');}};
  useEffect(()=>{load();},[]);
  const upcoming=useMemo(()=>visits.filter(v=>['SCHEDULED','ASSIGNED'].includes(v.status)&&v.scheduledAt),[visits]);
  const submit=async(e:React.FormEvent)=>{e.preventDefault();try{const v=await api.post<RoboCompanionVisit>('/api/robocompanion-requests',form);setMessage(`RoboCompanion Visit #${v.id} submitted`);setForm({...form,reason:'',assistanceNeeds:'',serviceNotes:''});await load();}catch(err){setError(err instanceof Error?err.message:'Unable to submit request');}};
  const cancel=async(id:number)=>{if(!window.confirm('Cancel this RoboCompanion Visit?'))return;try{await api.del(`/api/robocompanion-requests/${id}`);setMessage('RoboCompanion Visit cancelled');await load();}catch(e){setError(e instanceof Error?e.message:'Unable to cancel');}};
  return <section className="robo-area">
    <div className="section-heading"><span>Third service</span><h2>RoboCompanion Visit</h2></div>
    {message&&<p className="notice success">{message}</p>}{error&&<p className="notice error">{error}</p>}
    <section className="stat-grid"><article className="stat"><strong>{visits.filter(v=>!['COMPLETED','CANCELLED','REJECTED'].includes(v.status)).length}</strong><span>Active RoboCompanion visits</span></article><article className="stat"><strong>{upcoming.length}</strong><span>Upcoming robot visits</span></article></section>
    {upcoming.length>0&&<div className="card accent"><h2>Upcoming RoboCompanion Visits</h2>{upcoming.map(v=><p key={v.id}><strong>{new Date(v.scheduledAt!).toLocaleString()}</strong> · {v.reason}{v.assignedRoboCompanionName?` · ${v.assignedRoboCompanionName} (${v.assignedRoboCompanionModel})`:''}</p>)}</div>}
    <form className="card form-grid" onSubmit={submit}><div className="full section-heading"><span>Physical assistive robot</span><h2>Request a RoboCompanion Visit</h2></div><label>Date<input type="date" min={tomorrow()} required value={form.requestedDate} onChange={e=>setForm({...form,requestedDate:e.target.value})}/></label><label>Preferred time<input type="time" required value={form.requestedTime} onChange={e=>setForm({...form,requestedTime:e.target.value})}/></label><label className="full">Reason<input required maxLength={500} value={form.reason} onChange={e=>setForm({...form,reason:e.target.value})}/></label><label className="full">Assistance needs<textarea value={form.assistanceNeeds} onChange={e=>setForm({...form,assistanceNeeds:e.target.value})}/></label><label className="full">Service notes<textarea value={form.serviceNotes} onChange={e=>setForm({...form,serviceNotes:e.target.value})}/></label><button className="button full">Submit RoboCompanion Visit request</button></form>
    <section className="card"><div className="section-heading"><span>Tracking</span><h2>RoboCompanion Visit history</h2></div>{visits.length===0?<p className="muted">No RoboCompanion Visit requests yet.</p>:<div className="table-wrap"><table><thead><tr><th>Requested</th><th>Reason</th><th>Status</th><th>Scheduled</th><th>RoboCompanion</th><th>Notes</th><th></th></tr></thead><tbody>{visits.map(v=><tr key={v.id}><td>{v.requestedDate} {v.requestedTime}</td><td>{v.reason}</td><td><span className={`badge ${v.status.toLowerCase()}`}>{pretty(v.status)}</span></td><td>{v.scheduledAt?new Date(v.scheduledAt).toLocaleString():'—'}</td><td>{v.assignedRoboCompanionName?`${v.assignedRoboCompanionName} · ${v.assignedRoboCompanionModel}`:'—'}</td><td>{v.completionNotes||v.adminNotes||'—'}</td><td>{cancellable(v.status)&&<button className="text-button danger" onClick={()=>cancel(v.id)}>Cancel</button>}</td></tr>)}</tbody></table></div>}</section>
  </section>;
}
