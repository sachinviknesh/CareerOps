import React from 'react';
import { StatusBadge } from './StatusBadge';

export function OpportunityCard({ opportunity, onSelect, onApply }) {
  const { title, organization, type, location, deadline, roleFamily, requiredSkills = [] } = opportunity;

  const isUrgent = () => {
    if (!deadline) return false;
    const diffDays = Math.ceil((new Date(deadline) - new Date()) / (1000 * 60 * 60 * 24));
    return diffDays >= 0 && diffDays <= 5;
  };

  return (
    <div style={{
      backgroundColor: 'var(--bg-surface)',
      border: '1px solid var(--border-subtle)',
      borderRadius: 'var(--radius-md)',
      padding: 'var(--space-4)',
      boxShadow: 'var(--shadow-subtle)',
      display: 'flex',
      flexDirection: 'column',
      justifyContent: 'space-between',
      gap: 'var(--space-3)'
    }}>
      <div>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 'var(--space-2)' }}>
          <StatusBadge type={type} />
          {deadline && (
            <span className="mono" style={{
              fontSize: '11px',
              color: isUrgent() ? 'var(--status-danger-text)' : 'var(--text-secondary)',
              fontWeight: isUrgent() ? 600 : 400
            }}>
              {isUrgent() ? '⚠ Due ' : 'Due '}{deadline}
            </span>
          )}
        </div>
        <h4 style={{ fontSize: '15px', fontWeight: 600, color: 'var(--text-primary)', marginBottom: 'var(--space-1)' }}>
          {title}
        </h4>
        <div style={{ fontSize: '13px', color: 'var(--text-secondary)', marginBottom: 'var(--space-2)' }}>
          {organization || 'Independent'}{location ? ` • ${location}` : ''}
        </div>
        {roleFamily && (
          <div style={{ fontSize: '12px', color: 'var(--text-tertiary)', marginBottom: 'var(--space-2)' }}>
            Domain: {roleFamily}
          </div>
        )}
        {requiredSkills && requiredSkills.length > 0 && (
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '4px', marginTop: 'var(--space-2)' }}>
            {Array.from(requiredSkills).slice(0, 4).map((s) => (
              <span key={s.id} className="badge badge-neutral" style={{ fontSize: '10px' }}>
                {s.name}
              </span>
            ))}
            {requiredSkills.length > 4 && (
              <span className="mono" style={{ fontSize: '10px', color: 'var(--text-tertiary)' }}>
                +{requiredSkills.length - 4} more
              </span>
            )}
          </div>
        )}
      </div>

      <div style={{ display: 'flex', gap: 'var(--space-2)', paddingTop: 'var(--space-2)', borderTop: '1px solid var(--border-subtle)' }}>
        <button className="btn-secondary btn-sm" style={{ flex: 1 }} onClick={() => onSelect && onSelect(opportunity)}>
          View & Fit
        </button>
        {onApply && (
          <button className="btn-primary btn-sm" onClick={() => onApply(opportunity)}>
            Track
          </button>
        )}
      </div>
    </div>
  );
}
