import { Link } from 'react-router-dom';

export default function LandingPage() {
  return (
    <main className="landing">
      <section className="hero">
        <span className="eyebrow">Meals on Wheels Plus</span>
        <h1>Meals, practical help, and a little more connection.</h1>
        <p>One simple service for seniors to request meal deliveries and companion visits—and for caring volunteers to make them happen.</p>
        <Link to="/login" className="button">Sign in to your account</Link>
      </section>
      <section className="feature-grid" aria-label="Services">
        <article className="feature"><span>01</span><h2>Meal delivery</h2><p>Request meals, share dietary needs, and follow every delivery update.</p></article>
        <article className="feature"><span>02</span><h2>Companion visits</h2><p>Schedule friendly support for reminders, assistance, and companionship.</p></article>
        <article className="feature"><span>03</span><h2>Coordinated care</h2><p>Administrators and volunteers keep every request moving safely.</p></article>
      </section>
    </main>
  );
}
