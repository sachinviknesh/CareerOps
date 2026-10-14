import React, { useState } from 'react';
import { api, tokenStorage } from '../api/client';

export function Register({ onRegisterSuccess, onNavigate }) {
  const [name, setName] = useState('');
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

    const trimmedName = name.trim();
    const trimmedEmail = email.trim().toLowerCase();
    const newFieldErrors = {};

    if (!trimmedName) {
      newFieldErrors.name = 'Full name is required.';
    }
    if (!trimmedEmail) {
      newFieldErrors.email = 'Email address is required.';
    } else if (!/\S+@\S+\.\S+/.test(trimmedEmail)) {
      newFieldErrors.email = 'Please enter a valid email address.';
    }
    if (!password) {
      newFieldErrors.password = 'Password is required.';
    } else if (password.length < 6) {
      newFieldErrors.password = 'Password must be at least 6 characters long.';
    }

    if (Object.keys(newFieldErrors).length > 0) {
      setFieldErrors(newFieldErrors);
      setError('Please fix the highlighted input errors.');
      return;
    }

    setLoading(true);

    try {
      const res = await api.register({
        name: trimmedName,
        email: trimmedEmail,
        password
      });
      tokenStorage.set(res.token);
      onRegisterSuccess();
    } catch (err) {
      if (err.status === 409) {
        const msg = err.message || 'An account with this email already exists.';
        setError(msg);
        setFieldErrors({ email: msg });
      } else if (err.status === 400 && err.fieldErrors) {
        setFieldErrors(err.fieldErrors);
        setError(err.message || 'Please fix the highlighted input errors.');
      } else if (err.status === 503 || (err.message && (err.message.includes('port 8080') || err.message.includes('Cannot connect')))) {
        setError('Unable to connect to the backend server. Please verify the Spring Boot service is running on port 8080.');
      } else {
        setError(err.message || 'Registration failed. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{
      maxWidth: '420px',
      margin: '50px auto',
      backgroundColor: 'var(--bg-surface)',
      padding: 'var(--space-6)',
      borderRadius: 'var(--radius-lg)',
      border: '1px solid var(--border-subtle)',
      boxShadow: 'var(--shadow-card)'
    }}>
      <div style={{ textAlign: 'center', marginBottom: 'var(--space-5)' }}>
        <h2 style={{ fontSize: '20px', fontWeight: 600, marginBottom: 'var(--space-1)' }}>
          Create Candidate Profile
        </h2>
        <p style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>
          Start tracking opportunities and measuring your fit
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
          <label htmlFor="reg-name">Full Name</label>
          <input
            id="reg-name"
            type="text"
            required
            value={name}
            onChange={(e) => {
              setName(e.target.value);
              if (fieldErrors.name) setFieldErrors(prev => ({ ...prev, name: null }));
            }}
            placeholder="Jane Doe"
            disabled={loading}
          />
          {fieldErrors.name && (
            <div style={{ color: 'var(--color-danger, #ef4444)', fontSize: '12px', marginTop: '4px' }}>
              {fieldErrors.name}
            </div>
          )}
        </div>

        <div className="form-group">
          <label htmlFor="reg-email">Email Address</label>
          <input
            id="reg-email"
            type="email"
            required
            value={email}
            onChange={(e) => {
              setEmail(e.target.value);
              if (fieldErrors.email) setFieldErrors(prev => ({ ...prev, email: null }));
            }}
            placeholder="jane@example.com"
            disabled={loading}
          />
          {fieldErrors.email && (
            <div style={{ color: 'var(--color-danger, #ef4444)', fontSize: '12px', marginTop: '4px' }}>
              {fieldErrors.email}
            </div>
          )}
        </div>

        <div className="form-group">
          <label htmlFor="reg-password">Password (min 6 chars)</label>
          <input
            id="reg-password"
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
          {loading ? 'Creating Account...' : 'Register'}
        </button>
      </form>

      <div style={{ marginTop: 'var(--space-5)', textAlign: 'center', fontSize: '13px', color: 'var(--text-secondary)' }}>
        Already registered?{' '}
        <a href="#login" onClick={(e) => { e.preventDefault(); onNavigate('login'); }}>
          Sign In
        </a>
      </div>
    </div>
  );
}
