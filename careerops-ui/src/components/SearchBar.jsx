import React from 'react';

export function SearchBar({
  value,
  onChange,
  onSearch,
  placeholder = 'Search by title or keyword...',
  filters = null,
  actions = null
}) {
  const handleSubmit = (e) => {
    e.preventDefault();
    if (onSearch) onSearch();
  };

  return (
    <div style={{
      display: 'flex',
      flexWrap: 'wrap',
      gap: 'var(--space-3)',
      alignItems: 'center',
      justifyContent: 'space-between',
      marginBottom: 'var(--space-4)'
    }}>
      <form onSubmit={handleSubmit} style={{ display: 'flex', gap: 'var(--space-2)', flex: '1', minWidth: '280px', maxWidth: '500px' }}>
        <div style={{ position: 'relative', width: '100%' }}>
          <span className="material-symbols-outlined" style={{
            position: 'absolute',
            left: '10px',
            top: '50%',
            transform: 'translateY(-50%)',
            color: 'var(--text-tertiary)',
            pointerEvents: 'none'
          }}>
            search
          </span>
          <input
            type="text"
            value={value}
            onChange={(e) => onChange(e.target.value)}
            placeholder={placeholder}
            style={{ paddingLeft: '36px' }}
            aria-label="Search input"
          />
        </div>
        <button type="submit" className="btn-secondary">
          Search
        </button>
      </form>

      <div style={{ display: 'flex', gap: 'var(--space-2)', alignItems: 'center' }}>
        {filters}
        {actions}
      </div>
    </div>
  );
}
