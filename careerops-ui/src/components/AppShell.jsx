import React from 'react';
import { tokenStorage } from '../api/client';

export function AppShell({ activePage, onNavigate, user, children }) {
  const handleLogout = () => {
    tokenStorage.clear();
    onNavigate('login');
  };

  const navItems = [
    { id: 'dashboard', label: 'Dashboard', icon: 'dashboard' },
    { id: 'opportunities', label: 'Opportunities', icon: 'work' },
    { id: 'applications', label: 'Tracker', icon: 'description' },
    { id: 'profile', label: 'Profile & Skills', icon: 'person' },
    { id: 'resume', label: 'Resume & Quality', icon: 'upload_file' }
  ];

  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh', backgroundColor: 'var(--bg-app)' }}>
      {/* Top Bar */}
      <header style={{
        height: '56px',
        backgroundColor: 'var(--bg-surface)',
        borderBottom: '1px solid var(--border-subtle)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        padding: '0 var(--space-5)',
        position: 'sticky',
        top: 0,
        zIndex: 100,
        boxShadow: 'var(--shadow-subtle)'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)' }}>
          <div style={{
            width: '32px',
            height: '32px',
            borderRadius: 'var(--radius-sm)',
            backgroundColor: 'var(--accent-primary)',
            color: 'var(--text-inverse)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontWeight: 700,
            fontSize: '14px',
            letterSpacing: '-0.02em'
          }} className="mono">
            CO
          </div>
          <div>
            <span style={{ fontSize: '15px', fontWeight: 700, color: 'var(--text-primary)', letterSpacing: '-0.02em' }}>
              CareerOps
            </span>
            <span style={{ fontSize: '11px', color: 'var(--text-tertiary)', marginLeft: 'var(--space-2)' }} className="mono">
              Opportunity & Job-Fit Tracker
            </span>
          </div>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-4)' }}>
          {user ? (
            <>
              <div style={{ textAlign: 'right', fontSize: '12px' }}>
                <div style={{ fontWeight: 600, color: 'var(--text-primary)' }}>{user.name}</div>
                <div style={{ color: 'var(--text-tertiary)' }}>{user.email}</div>
              </div>
              <button
                className="btn-secondary btn-sm"
                onClick={handleLogout}
                title="Logout"
                style={{ display: 'flex', alignItems: 'center', gap: '4px' }}
              >
                <span className="material-symbols-outlined" style={{ fontSize: '16px' }}>logout</span>
                Logout
              </button>
            </>
          ) : (
            <div style={{ display: 'flex', gap: 'var(--space-2)' }}>
              <button className="btn-secondary btn-sm" onClick={() => onNavigate('login')}>Login</button>
              <button className="btn-primary btn-sm" onClick={() => onNavigate('register')}>Register</button>
            </div>
          )}
        </div>
      </header>

      {/* Main Container: Sidebar + Content */}
      <div style={{ display: 'flex', flex: 1 }}>
        {/* Left Navigation Rail */}
        <aside style={{
          width: '230px',
          backgroundColor: 'var(--bg-surface)',
          borderRight: '1px solid var(--border-subtle)',
          padding: 'var(--space-4) var(--space-2)',
          display: 'flex',
          flexDirection: 'column',
          gap: 'var(--space-1)'
        }}>
          {navItems.map((item) => {
            const isActive = activePage === item.id;
            return (
              <button
                key={item.id}
                onClick={() => onNavigate(item.id)}
                style={{
                  width: '100%',
                  justifyContent: 'flex-start',
                  padding: 'var(--space-2) var(--space-3)',
                  borderRadius: 'var(--radius-sm)',
                  border: '1px solid transparent',
                  backgroundColor: isActive ? 'var(--accent-subtle)' : 'transparent',
                  color: isActive ? 'var(--accent-primary)' : 'var(--text-secondary)',
                  fontWeight: isActive ? 600 : 500,
                  fontSize: '13px',
                  display: 'flex',
                  alignItems: 'center',
                  gap: 'var(--space-3)',
                  transition: 'all 0.15s ease'
                }}
              >
                <span className="material-symbols-outlined" style={{
                  fontSize: '20px',
                  color: isActive ? 'var(--accent-primary)' : 'var(--text-tertiary)'
                }}>
                  {item.icon}
                </span>
                {item.label}
              </button>
            );
          })}
        </aside>

        {/* Content Body */}
        <main style={{
          flex: 1,
          padding: 'var(--space-6)',
          maxWidth: '1280px',
          margin: '0 auto',
          width: '100%'
        }}>
          {children}
        </main>
      </div>
    </div>
  );
}
