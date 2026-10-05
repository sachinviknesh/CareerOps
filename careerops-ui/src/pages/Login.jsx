import React, { useState } from 'react';
import { api, tokenStorage } from '../api/client';

export function Login({ onLoginSuccess, onNavigate }) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      const res = await api.login({ email, password });
      tokenStorage.set(res.token);
      onLoginSuccess();
    } catch (err) {
      setError(err.message || 'Authentication failed. Please verify credentials.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{
      maxWidth: '400px',
      margin: '60px auto',
      backgroundColor: 'var(--bg-surface)',
      padding: 'var(--space-6)',
      borderRadius: 'var(--radius-lg)',
      border: '1px solid var(--border-subtle)',
      boxShadow: 'var(--shadow-card)'
    }}>
      <div style={{ textAlign: 'center', marginBottom: 'var(--space-5)' }}>
        <h2 style={{ fontSize: '20px', fontWeight: 600, marginBottom: 'var(--space-1)' }}>
          Sign In to CareerOps
        </h2>
        <p style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>
          Opportunity & Job-Fit Tracking Console
        </p>
      </div>

      {error && (
        <div className="alert alert-danger">
          <span className="material-symbols-outlined">error</span>
          <div>{error}</div>
        </div>
      )}

      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="email">Email Address</label>
          <input
            id="email"
            type="email"
            required
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="candidate@example.com"
          />
        </div>

        <div className="form-group">
          <label htmlFor="password">Password</label>
          <input
            id="password"
            type="password"
            required
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="••••••••"
          />
        </div>

        <button
          type="submit"
          className="btn-primary"
          style={{ width: '100%', marginTop: 'var(--space-2)' }}
          disabled={loading}
        >
          {loading ? 'Authenticating...' : 'Sign In'}
        </button>
      </form>

      <div style={{ marginTop: 'var(--space-5)', textAlign: 'center', fontSize: '13px', color: 'var(--text-secondary)' }}>
        Don't have an account?{' '}
        <a href="#register" onClick={(e) => { e.preventDefault(); onNavigate('register'); }}>
          Create profile
        </a>
      </div>
    </div>
  );
}
