import React from 'react';

export function EmptyState({ icon = 'inbox', title, description, actionText, onAction }) {
  return (
    <div style={{
      textAlign: 'center',
      padding: 'var(--space-7) var(--space-4)',
      backgroundColor: 'var(--bg-surface)',
      borderRadius: 'var(--radius-md)',
      border: '1px dashed var(--border-strong)',
      margin: 'var(--space-4) 0'
    }}>
      <div style={{
        width: '48px',
        height: '48px',
        borderRadius: '50%',
        backgroundColor: 'var(--bg-surface-subtle)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        margin: '0 auto var(--space-3)',
        color: 'var(--text-tertiary)'
      }}>
        <span className="material-symbols-outlined" style={{ fontSize: '28px' }}>{icon}</span>
      </div>
      <h3 style={{ fontSize: '16px', fontWeight: 600, marginBottom: 'var(--space-1)' }}>
        {title}
      </h3>
      {description && (
        <p style={{ fontSize: '13px', color: 'var(--text-secondary)', maxWidth: '420px', margin: '0 auto var(--space-4)' }}>
          {description}
        </p>
      )}
      {actionText && onAction && (
        <button className="btn-primary btn-sm" onClick={onAction}>
          {actionText}
        </button>
      )}
    </div>
  );
}
