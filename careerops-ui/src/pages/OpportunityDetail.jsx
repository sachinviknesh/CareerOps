import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import { StatusBadge } from '../components/StatusBadge';
import { FitPanel } from '../components/FitPanel';

export function OpportunityDetail({ opportunityId, onNavigate }) {
  const [opportunity, setOpportunity] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Fit & AI state
  const [fitData, setFitData] = useState(null);
  const [fitLoading, setFitLoading] = useState(false);
  const [fitError, setFitError] = useState(null);

  const [aiReview, setAiReview] = useState(null);
  const [aiLoading, setAiLoading] = useState(false);

  // Tracking state
  const [trackingLoading, setTrackingLoading] = useState(false);
  const [trackingSuccess, setTrackingSuccess] = useState(null);

  useEffect(() => {
    if (opportunityId) {
      loadOpportunity();
    }
  }, [opportunityId]);

  const loadOpportunity = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await api.getOpportunity(opportunityId);
      setOpportunity(data);
      // Automatically calculate offline fit if opportunity loaded
      handleAnalyzeFit();
    } catch (err) {
      setError(err.message || 'Failed to load opportunity details');
    } finally {
      setLoading(false);
    }
  };

  const handleAnalyzeFit = async () => {
    setFitLoading(true);
    setFitError(null);
    try {
      const fit = await api.getJobFit(opportunityId);
      setFitData(fit);
    } catch (err) {
      setFitError(err.message || 'Could not analyze job fit. Ensure your profile or resume is updated.');
    } finally {
      setFitLoading(false);
    }
  };

  const handleAiReview = async () => {
    setAiLoading(true);
    try {
      const review = await api.getAiReview(opportunityId);
      setAiReview(review);
    } catch (err) {
      alert(err.message || 'AI review failed or is disabled in configuration.');
    } finally {
      setAiLoading(false);
    }
  };

  const handleAddToTracker = async (status = 'TO_APPLY') => {
    setTrackingLoading(true);
    setTrackingSuccess(null);
    try {
      await api.createApplication({
        opportunityId: Number(opportunityId),
        status,
        notes: `Saved from opportunity detail page. Match score: ${fitData ? fitData.score + '%' : 'uncalculated'}`
      });
      setTrackingSuccess(`Opportunity successfully added to tracker with status "${status}"!`);
    } catch (err) {
      alert(err.message || 'Failed to add to tracker. You may already be tracking this opportunity.');
    } finally {
      setTrackingLoading(false);
    }
  };

  if (loading) {
    return (
      <div style={{ padding: 'var(--space-7)', textAlign: 'center', color: 'var(--text-secondary)' }}>
        Loading opportunity details...
      </div>
    );
  }

  if (error || !opportunity) {
    return (
      <div>
        <button className="btn-secondary btn-sm" onClick={() => onNavigate('opportunities')} style={{ marginBottom: 'var(--space-4)' }}>
          <span className="material-symbols-outlined">arrow_back</span> Back to Opportunities
        </button>
        <div className="alert alert-danger">{error || 'Opportunity not found'}</div>
      </div>
    );
  }

  const { title, organization, type, roleFamily, description, location, workMode, deadline, url, requiredSkills = [] } = opportunity;

  return (
    <div>
      {/* Back button */}
      <button
        className="btn-secondary btn-sm"
        onClick={() => onNavigate('opportunities')}
        style={{ marginBottom: 'var(--space-4)' }}
      >
        <span className="material-symbols-outlined" style={{ fontSize: '16px' }}>arrow_back</span>
        Back to Opportunities
      </button>

      {trackingSuccess && (
        <div className="alert alert-success">
          <span className="material-symbols-outlined">check_circle</span>
          <div>{trackingSuccess}</div>
        </div>
      )}

      {/* Main Split: Left Details, Right Fit & AI Analysis */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(360px, 1fr))', gap: 'var(--space-6)', alignItems: 'start' }}>
        {/* Left Column: Opportunity Details */}
        <div style={{
          backgroundColor: 'var(--bg-surface)',
          border: '1px solid var(--border-subtle)',
          borderRadius: 'var(--radius-lg)',
          padding: 'var(--space-6)',
          boxShadow: 'var(--shadow-card)'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)', marginBottom: 'var(--space-2)' }}>
            <StatusBadge type={type} />
            {roleFamily && (
              <span className="badge badge-neutral">{roleFamily}</span>
            )}
          </div>

          <h2 style={{ fontSize: '24px', fontWeight: 600, color: 'var(--text-primary)', marginBottom: 'var(--space-1)' }}>
            {title}
          </h2>
          <div style={{ fontSize: '15px', color: 'var(--text-secondary)', marginBottom: 'var(--space-4)' }}>
            {organization || 'Independent Organization'}
          </div>

          {/* Meta grid */}
          <div style={{
            display: 'grid',
            gridTemplateColumns: '1fr 1fr',
            gap: 'var(--space-3)',
            padding: 'var(--space-3)',
            backgroundColor: 'var(--bg-surface-subtle)',
            borderRadius: 'var(--radius-md)',
            marginBottom: 'var(--space-5)',
            fontSize: '13px'
          }}>
            <div>
              <span style={{ color: 'var(--text-tertiary)', display: 'block', fontSize: '11px' }}>LOCATION</span>
              <strong>{location || 'Remote / Not specified'}</strong>
            </div>
            <div>
              <span style={{ color: 'var(--text-tertiary)', display: 'block', fontSize: '11px' }}>WORK MODE</span>
              <strong>{workMode || 'Flexible'}</strong>
            </div>
            <div>
              <span style={{ color: 'var(--text-tertiary)', display: 'block', fontSize: '11px' }}>DEADLINE</span>
              <strong className="mono">{deadline || 'Rolling basis'}</strong>
            </div>
            <div>
              <span style={{ color: 'var(--text-tertiary)', display: 'block', fontSize: '11px' }}>EXTERNAL LINK</span>
              {url ? (
                <a href={url} target="_blank" rel="noreferrer" style={{ display: 'inline-flex', alignItems: 'center', gap: '2px' }}>
                  Visit Listing <span className="material-symbols-outlined" style={{ fontSize: '14px' }}>open_in_new</span>
                </a>
              ) : (
                <span style={{ color: 'var(--text-tertiary)' }}>None</span>
              )}
            </div>
          </div>

          {/* Required Skills */}
          <div style={{ marginBottom: 'var(--space-5)' }}>
            <h4 style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: 'var(--space-2)' }}>
              Required Skills Specified
            </h4>
            {requiredSkills && requiredSkills.length > 0 ? (
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px' }}>
                {Array.from(requiredSkills).map(s => (
                  <span key={s.id} className="badge badge-neutral" style={{ padding: '3px 8px' }}>
                    {s.name}
                  </span>
                ))}
              </div>
            ) : (
              <span style={{ fontSize: '13px', color: 'var(--text-tertiary)' }}>No specific skills enumerated.</span>
            )}
          </div>

          {/* Job Description */}
          <div style={{ marginBottom: 'var(--space-6)' }}>
            <h4 style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: 'var(--space-2)' }}>
              Role Description
            </h4>
            <div style={{
              fontSize: '14px',
              lineHeight: 1.6,
              color: 'var(--text-primary)',
              whiteSpace: 'pre-wrap',
              maxHeight: '350px',
              overflowY: 'auto',
              padding: 'var(--space-3)',
              backgroundColor: 'var(--bg-surface-subtle)',
              borderRadius: 'var(--radius-sm)'
            }}>
              {description || 'No detailed description provided.'}
            </div>
          </div>

          {/* Action Area */}
          <div style={{
            display: 'flex',
            flexWrap: 'wrap',
            gap: 'var(--space-3)',
            paddingTop: 'var(--space-4)',
            borderTop: '1px solid var(--border-subtle)'
          }}>
            <button
              className="btn-primary"
              onClick={() => handleAddToTracker('TO_APPLY')}
              disabled={trackingLoading}
            >
              <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>bookmark</span>
              Save to Tracker
            </button>
            <button
              className="btn-secondary"
              onClick={() => handleAddToTracker('APPLIED')}
              disabled={trackingLoading}
            >
              <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>send</span>
              Mark as Applied
            </button>
            <button
              className="btn-secondary"
              onClick={handleAnalyzeFit}
              disabled={fitLoading}
            >
              <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>sync</span>
              Re-Calculate Fit
            </button>
          </div>
        </div>

        {/* Right Column: Explainable Fit Panel & Grounded AI Analysis */}
        <div style={{
          backgroundColor: 'var(--bg-surface)',
          border: '1px solid var(--border-subtle)',
          borderRadius: 'var(--radius-lg)',
          padding: 'var(--space-6)',
          boxShadow: 'var(--shadow-card)'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 'var(--space-4)' }}>
            <h3 style={{ fontSize: '17px', fontWeight: 600 }}>Candidate Fit Engine</h3>
            <span className="mono badge badge-neutral" style={{ fontSize: '11px' }}>Offline Heuristic</span>
          </div>

          <FitPanel
            fitData={fitData}
            loading={fitLoading}
            error={fitError}
            onAiReview={handleAiReview}
            aiLoading={aiLoading}
            aiReview={aiReview}
          />
        </div>
      </div>
    </div>
  );
}
