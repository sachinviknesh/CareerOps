import React, { useState, useEffect } from 'react';
import { AppShell } from './components/AppShell';
import { Login } from './pages/Login';
import { Register } from './pages/Register';
import { Dashboard } from './pages/Dashboard';
import { Opportunities } from './pages/Opportunities';
import { OpportunityDetail } from './pages/OpportunityDetail';
import { BestMatches } from './pages/BestMatches';
import { Applications } from './pages/Applications';
import { Profile } from './pages/Profile';
import { Resume } from './pages/Resume';
import { api, tokenStorage } from './api/client';

export function App() {
  const [currentPage, setCurrentPage] = useState('dashboard');
  const [selectedOpportunityId, setSelectedOpportunityId] = useState(null);
  const [currentUser, setCurrentUser] = useState(null);
  const [authChecking, setAuthChecking] = useState(true);

  // Sync route with window.location.hash or history
  useEffect(() => {
    const handleHashChange = () => {
      const hash = window.location.hash.replace('#', '') || 'dashboard';
      if (hash.startsWith('opportunities/')) {
        const id = hash.split('/')[1];
        setSelectedOpportunityId(id);
        setCurrentPage('opportunityDetail');
      } else {
        setCurrentPage(hash);
      }
    };

    window.addEventListener('hashchange', handleHashChange);
    handleHashChange();
    checkAuth();

    return () => window.removeEventListener('hashchange', handleHashChange);
  }, []);

  const checkAuth = async () => {
    if (!tokenStorage.has()) {
      setAuthChecking(false);
      if (currentPage !== 'register') {
        navigateTo('login');
      }
      return;
    }

    try {
      const user = await api.getProfile();
      setCurrentUser(user);
    } catch (e) {
      tokenStorage.clear();
      setCurrentUser(null);
      navigateTo('login');
    } finally {
      setAuthChecking(false);
    }
  };

  const navigateTo = (page, param = null) => {
    if (page === 'opportunityDetail' && param) {
      setSelectedOpportunityId(param);
      window.location.hash = `opportunities/${param}`;
      setCurrentPage('opportunityDetail');
    } else {
      window.location.hash = page;
      setCurrentPage(page);
    }
  };

  const handleLoginSuccess = () => {
    checkAuth();
    navigateTo('dashboard');
  };

  if (authChecking) {
    return (
      <div style={{
        height: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        fontFamily: 'var(--font-sans)',
        color: 'var(--text-secondary)'
      }}>
        Initializing CareerOps Console...
      </div>
    );
  }

  // Unauthenticated view
  if (!tokenStorage.has()) {
    return (
      <div style={{ minHeight: '100vh', backgroundColor: 'var(--bg-app)', display: 'flex', flexDirection: 'column' }}>
        <header style={{
          padding: 'var(--space-4) var(--space-6)',
          borderBottom: '1px solid var(--border-subtle)',
          backgroundColor: 'var(--bg-surface)',
          display: 'flex',
          alignItems: 'center',
          gap: 'var(--space-3)'
        }}>
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
            fontSize: '14px'
          }} className="mono">
            CO
          </div>
          <div>
            <strong style={{ fontSize: '15px' }}>CareerOps</strong>
            <span style={{ fontSize: '12px', color: 'var(--text-secondary)', marginLeft: 'var(--space-2)' }}>
              Opportunity & Job-Fit Tracker
            </span>
          </div>
        </header>

        <main style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          {currentPage === 'register' ? (
            <Register onRegisterSuccess={handleLoginSuccess} onNavigate={navigateTo} />
          ) : (
            <Login onLoginSuccess={handleLoginSuccess} onNavigate={navigateTo} />
          )}
        </main>
      </div>
    );
  }

  // Authenticated Workspace
  return (
    <AppShell activePage={currentPage} onNavigate={navigateTo} user={currentUser}>
      {currentPage === 'dashboard' && (
        <Dashboard onNavigate={navigateTo} onSelectOpportunity={(id) => navigateTo('opportunityDetail', id)} />
      )}
      {currentPage === 'opportunities' && (
        <Opportunities onNavigate={navigateTo} onSelectOpportunity={(id) => navigateTo('opportunityDetail', id)} />
      )}
      {currentPage === 'best-matches' && (
        <BestMatches onNavigate={navigateTo} onSelectOpportunity={(id) => navigateTo('opportunityDetail', id)} />
      )}
      {currentPage === 'opportunityDetail' && (
        <OpportunityDetail opportunityId={selectedOpportunityId} onNavigate={navigateTo} />
      )}
      {currentPage === 'applications' && (
        <Applications onNavigate={navigateTo} onSelectOpportunity={(id) => navigateTo('opportunityDetail', id)} />
      )}
      {currentPage === 'profile' && (
        <Profile onNavigate={navigateTo} />
      )}
      {currentPage === 'resume' && (
        <Resume onNavigate={navigateTo} />
      )}
    </AppShell>
  );
}
export default App;
