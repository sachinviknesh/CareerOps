import React from 'react';

export function Pagination({ page, totalPages, totalElements, onPageChange }) {
  if (totalPages <= 1) return null;

  return (
    <div style={{
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      padding: 'var(--space-3) var(--space-4)',
      borderTop: '1px solid var(--border-subtle)',
      backgroundColor: 'var(--bg-surface-subtle)',
      fontSize: '13px',
      color: 'var(--text-secondary)'
    }}>
      <div>
        Showing page <span className="mono" style={{ fontWeight: 600, color: 'var(--text-primary)' }}>{page + 1}</span> of <span className="mono">{totalPages}</span> {totalElements !== undefined && `(${totalElements} total)`}
      </div>
      <div style={{ display: 'flex', gap: 'var(--space-2)' }}>
        <button
          className="btn-secondary btn-sm"
          disabled={page <= 0}
          onClick={() => onPageChange(page - 1)}
          aria-label="Previous Page"
        >
          <span className="material-symbols-outlined" style={{ fontSize: '16px' }}>arrow_back</span>
          Previous
        </button>
        <button
          className="btn-secondary btn-sm"
          disabled={page >= totalPages - 1}
          onClick={() => onPageChange(page + 1)}
          aria-label="Next Page"
        >
          Next
          <span className="material-symbols-outlined" style={{ fontSize: '16px' }}>arrow_forward</span>
        </button>
      </div>
    </div>
  );
}
