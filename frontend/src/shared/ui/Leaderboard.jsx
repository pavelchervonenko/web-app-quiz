import { Trophy } from 'lucide-react';

export function Leaderboard({ leaderboard }) {
  const entries = leaderboard?.entries || [];

  return (
    <section className="panel">
      <div className="panel-heading">
        <div>
          <p className="eyebrow">Результаты</p>
          <h2>Лидерборд</h2>
        </div>
        <Trophy size={22} />
      </div>

      {entries.length === 0 ? (
        <p className="muted">Пока нет участников.</p>
      ) : (
        <div className="leaderboard">
          {entries.map((entry) => (
            <div className="leaderboard-row" key={entry.participantSessionId}>
              <span className="rank">{entry.rank}</span>
              <span>{entry.displayName}</span>
              <strong>{entry.score}</strong>
            </div>
          ))}
        </div>
      )}
    </section>
  );
}
