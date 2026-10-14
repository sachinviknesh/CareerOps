import React, { useState } from 'react';
import { api, tokenStorage } from '../api/client';

export function Login({ onLoginSuccess, onNavigate }) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [fieldErrors, setFieldErrors] = useState({});

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (loading) return;

    setError(null);
    setFieldErrors({});

    const trimmedEmail = email.trim();
    const newFieldErrors = {};

    if (!trimmedEmail) {
      newFieldErrors.email = 'Email address is required.';
    } else if (!/\S+@\S+\.\S+/.test(trimmedEmail)) {
      newFieldErrors.email = 'Please enter a valid email address.';
    }

    if (!password) {
      newFieldErrors.password = 'Password is required.';
    }

    if (Object.keys(newFieldErrors).length > 0) {
      setFieldErrors(newFieldErrors);
      setError('Please provide your email and password.');
      return;
    }

    setLoading(true);

    try {
      const res = await api.login({ email: trimmedEmail, password });
      tokenStorage.set(res.token);
      onLoginSuccess();
    } catch (err) {
      if (err.status === 401) {
        setError(err.message || 'Invalid email or password.');
      } else if (err.status === 400 && err.fieldErrors) {
        setFieldErrors(err.fieldErrors);
        setError(err.message || 'Please fix the highlighted input errors.');
      } else if (err.status === 503 || (err.message && (err.message.includes('port 8080') || err.message.includes('Cannot connect')))) {
        setError('Cannot connect to backend server. Please verify the Spring Boot service is running on port 8080.');
      } else {
        setError(err.message || 'Authentication failed. Please verify credentials.');
      }
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
        <div className="alert alert-danger" style={{ marginBottom: 'var(--space-4)' }}>
          <span className="material-symbols-outlined">error</span>
          <div>{error}</div>
        </div>
      )}

      <form onSubmit={handleSubmit} noValidate>
        <div className="form-group">
          <label htmlFor="email">Email Address</label>
          <input
            id="email"
            type="email"
            required
            value={email}
            onChange={(e) => {
              setEmail(e.target.value);
              if (fieldErrors.email) setFieldErrors(prev => ({ ...prev, email: null }));
            }}
            placeholder="candidate@example.com"
            disabled={loading}
          />
          {fieldErrors.email && (
            <div style={{ color: 'var(--color-danger, #ef4444)', fontSize: '12px', marginTop: '4px' }}>
              {fieldErrors.email}
            </div>
          )}
        </div>

        <div className="form-group">
          <label htmlFor="password">Password</label>
          <input
            id="password"
            type="password"
            required
            value={password}
            onChange={(e) => {
              setPassword(e.target.value);
              if (fieldErrors.password) setFieldErrors(prev => ({ ...prev, password: null }));
            }}
            placeholder="••••••••"
            disabled={loading}
          />
          {fieldErrors.password && (
            <div style={{ color: 'var(--color-danger, #ef4444)', fontSize: '12px', marginTop: '4px' }}>
              {fieldErrors.password}
            </div>
          )}
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

      <div style={{
        marginTop: 'var(--space-4)',
        padding: '12px',
        backgroundColor: 'var(--bg-subtle, rgba(255,255,255,0.04))',
        border: '1px solid var(--border-subtle, #e2e8f0)',
        borderRadius: 'var(--radius-md, 8px)',
        fontSize: '12px',
        color: 'var(--text-secondary)',
        display: 'flex',
        flexDirection: 'column',
        gap: '6px'
      }}>
        <div style={{ fontWeight: 600, color: 'var(--text-primary)' }}>Demo Accounts:</div>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <span><code>sachin6002@gmail.com</code></span>
          <button
            type="button"
            className="btn-secondary"
            style={{ padding: '3px 8px', fontSize: '11px', height: 'auto', minHeight: 'unset' }}
            onClick={() => { setEmail('sachin6002@gmail.com'); setPassword('password123'); }}
          >
            Auto-fill
          </button>
        </div>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <span><code>candidate@careerops.dev</code></span>
          <button
            type="button"
            className="btn-secondary"
            style={{ padding: '3px 8px', fontSize: '11px', height: 'auto', minHeight: 'unset' }}
            onClick={() => { setEmail('candidate@careerops.dev'); setPassword('password123'); }}
          >
            Auto-fill
          </button>
        </div>
        <div style={{ fontSize: '11px', color: 'var(--text-tertiary, #94a3b8)', marginTop: '2px' }}>
          Default password: <code>password123</code>
        </div>
      </div>

      <div style={{ marginTop: 'var(--space-5)', textAlign: 'center', fontSize: '13px', color: 'var(--text-secondary)' }}>
        Don't have an account?{' '}
        <a href="#register" onClick={(e) => { e.preventDefault(); onNavigate('register'); }}>
          Create profile
        </a>
      </div>
    </div>
  );
}
