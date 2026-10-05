import React, { useState, useEffect } from 'react';
import { api } from '../api/client';

export function Resume({ onNavigate }) {
  const [profile, setProfile] = useState(null);
  const [analysis, setAnalysis] = useState(null);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  useEffect(() => {
    loadResumeData();
  }, []);

  const loadResumeData = async () => {
    setLoading(true);
    setError(null);
    try {
      const prof = await api.getProfile();
      setProfile(prof);

      if (prof && prof.hasResume) {
        const quality = await api.getResumeAnalysis();
        setAnalysis(quality);
      }
    } catch (err) {
      setError(err.message || 'Failed to load resume analysis');
    } finally {
      setLoading(false);
    }
  };

  const handleFileUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    const allowed = ['application/pdf', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 'application/msword'];
    if (!allowed.includes(file.type) && !file.name.endsWith('.pdf') && !file.name.endsWith('.docx')) {
      setError('Only PDF and DOCX resume formats are supported.');
      return;
    }

    setUploading(true);
    setError(null);
    setSuccess(null);

    try {
      const updatedProfile = await api.uploadResume(file);
      setProfile(updatedProfile);
      setSuccess(`Resume "${file.name}" successfully parsed with Apache Tika!`);
      // Reload quality analysis
      const quality = await api.getResumeAnalysis();
      setAnalysis(quality);
    } catch (err) {
      setError(err.message || 'Failed to parse resume.');
    } finally {
      setUploading(false);
    }
  };

  if (loading) {
    return <div style={{ padding: 'var(--space-6)', textAlign: 'center' }}>Loading resume details...</div>;
  }

  return (
    <div>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--space-5)' }}>
        <div>
          <h2 style={{ fontSize: '22px', fontWeight: 600 }}>Resume & Offline Quality Audit</h2>
          <p style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>
            Apache Tika document extraction with structural, lexical, and evidence quality heuristics
          </p>
        </div>
        <button className="btn-secondary btn-sm" onClick={() => onNavigate('dashboard')}>
          Dashboard
        </button>
      </div>

      {success && (
        <div className="alert alert-success">
          <span className="material-symbols-outlined">check_circle</span>
          <div>{success}</div>
        </div>
      )}

      {error && (
        <div className="alert alert-danger">
          <span className="material-symbols-outlined">error</span>
          <div>{error}</div>
        </div>
      )}

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(350px, 1fr))', gap: 'var(--space-6)' }}>
        {/* Left Column: Upload & Document Status */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-5)' }}>
          <div style={{
            backgroundColor: 'var(--bg-surface)',
            border: '1px solid var(--border-subtle)',
            borderRadius: 'var(--radius-lg)',
            padding: 'var(--space-6)',
            boxShadow: 'var(--shadow-card)'
          }}>
            <h3 style={{ fontSize: '16px', fontWeight: 600, marginBottom: 'var(--space-2)' }}>
              Resume Document
            </h3>
            <p style={{ fontSize: '13px', color: 'var(--text-secondary)', marginBottom: 'var(--space-4)' }}>
              Upload your latest PDF or DOCX file. Text is parsed securely offline on the backend.
            </p>

            {profile && profile.hasResume ? (
              <div style={{
                padding: 'var(--space-3)',
                backgroundColor: 'var(--status-success-bg)',
                borderRadius: 'var(--radius-sm)',
                border: '1px solid var(--status-success-border)',
                marginBottom: 'var(--space-4)',
                display: 'flex',
                alignItems: 'center',
                gap: 'var(--space-3)'
              }}>
                <span className="material-symbols-outlined" style={{ color: 'var(--status-success-text)' }}>
                  description
                </span>
                <div>
                  <div style={{ fontWeight: 600, fontSize: '13px', color: 'var(--status-success-text)' }}>
                    Active: {profile.resumeFileName || 'Extracted Resume'}
                  </div>
                  <div style={{ fontSize: '11px', color: 'var(--text-secondary)' }}>
                    Text stored in candidate record • Available for offline job fit
                  </div>
                </div>
              </div>
            ) : (
              <div style={{
                padding: 'var(--space-3)',
                backgroundColor: 'var(--status-warning-bg)',
                borderRadius: 'var(--radius-sm)',
                border: '1px solid var(--status-warning-border)',
                marginBottom: 'var(--space-4)',
                fontSize: '12px',
                color: 'var(--status-warning-text)'
              }}>
                No resume uploaded yet. Upload a PDF/DOCX to enable resume evidence matching.
              </div>
            )}

            <div style={{
              border: '2px dashed var(--border-strong)',
              borderRadius: 'var(--radius-md)',
              padding: 'var(--space-6)',
              textAlign: 'center',
              backgroundColor: 'var(--bg-surface-subtle)'
            }}>
              <span className="material-symbols-outlined" style={{ fontSize: '32px', color: 'var(--text-tertiary)', marginBottom: 'var(--space-2)' }}>
                upload_file
              </span>
              <div style={{ fontSize: '13px', fontWeight: 500, marginBottom: 'var(--space-2)' }}>
                Select PDF or DOCX resume
              </div>
              <input
                type="file"
                accept=".pdf,.docx,.doc,application/pdf,application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                onChange={handleFileUpload}
                disabled={uploading}
                style={{ maxWidth: '280px', margin: '0 auto', fontSize: '12px' }}
              />
              {uploading && (
                <div style={{ fontSize: '12px', color: 'var(--accent-primary)', marginTop: 'var(--space-2)' }}>
                  Extracting text with Apache Tika...
                </div>
              )}
            </div>
          </div>

          {/* Section presence list */}
          {analysis && analysis.sections && (
            <div style={{
              backgroundColor: 'var(--bg-surface)',
              border: '1px solid var(--border-subtle)',
              borderRadius: 'var(--radius-lg)',
              padding: 'var(--space-5)',
              boxShadow: 'var(--shadow-subtle)'
            }}>
              <h4 style={{ fontSize: '14px', fontWeight: 600, marginBottom: 'var(--space-3)' }}>
                Detected Resume Structural Sections
              </h4>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 'var(--space-2)' }}>
                {Object.entries(analysis.sections).map(([sec, present]) => (
                  <div key={sec} style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: 'var(--space-2)',
                    fontSize: '12px',
                    padding: 'var(--space-1) var(--space-2)',
                    borderRadius: 'var(--radius-sm)',
                    backgroundColor: present ? 'var(--status-success-bg)' : 'var(--bg-surface-subtle)',
                    color: present ? 'var(--status-success-text)' : 'var(--text-tertiary)'
                  }}>
                    <span className="material-symbols-outlined" style={{ fontSize: '16px' }}>
                      {present ? 'check' : 'remove'}
                    </span>
                    {sec}
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* Right Column: Offline Quality Analysis Breakdown */}
        <div style={{
          backgroundColor: 'var(--bg-surface)',
          border: '1px solid var(--border-subtle)',
          borderRadius: 'var(--radius-lg)',
          padding: 'var(--space-6)',
          boxShadow: 'var(--shadow-card)'
        }}>
          <h3 style={{ fontSize: '16px', fontWeight: 600, marginBottom: 'var(--space-4)' }}>
            Quality Signals & Recommendations
          </h3>

          {!analysis ? (
            <div style={{ padding: 'var(--space-6)', textAlign: 'center', color: 'var(--text-tertiary)', fontSize: '13px' }}>
              Upload a resume to view offline section detection, metric presence, and technical coverage checks.
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-5)' }}>
              {/* Word Count Metric */}
              <div style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                padding: 'var(--space-3) var(--space-4)',
                backgroundColor: 'var(--bg-surface-subtle)',
                borderRadius: 'var(--radius-md)'
              }}>
                <div>
                  <span style={{ fontSize: '11px', color: 'var(--text-tertiary)', textTransform: 'uppercase' }}>EXTRACTED VOLUME</span>
                  <div style={{ fontSize: '14px', fontWeight: 600 }}>{analysis.wordCount} words detected</div>
                </div>
                <span className={`badge ${analysis.wordCount >= 150 ? 'badge-success' : 'badge-warning'}`}>
                  {analysis.wordCount >= 150 ? 'Sufficient Depth' : 'Brief / Sparse'}
                </span>
              </div>

              {/* Technical terms found */}
              {analysis.technicalTermsFound && analysis.technicalTermsFound.length > 0 && (
                <div>
                  <h4 style={{ fontSize: '13px', fontWeight: 600, marginBottom: 'var(--space-2)' }}>
                    Recognized Technical Terms ({analysis.technicalTermsFound.length})
                  </h4>
                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: '4px' }}>
                    {analysis.technicalTermsFound.map((term, idx) => (
                      <span key={idx} className="badge badge-neutral" style={{ textTransform: 'capitalize' }}>
                        {term}
                      </span>
                    ))}
                  </div>
                </div>
              )}

              {/* Strengths */}
              {analysis.strengths && analysis.strengths.length > 0 && (
                <div>
                  <h4 style={{ fontSize: '13px', fontWeight: 600, color: 'var(--status-success-text)', marginBottom: 'var(--space-2)', display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>check_circle</span>
                    Positive Signals
                  </h4>
                  <ul style={{ paddingLeft: 'var(--space-4)', fontSize: '13px', color: 'var(--text-secondary)' }}>
                    {analysis.strengths.map((str, idx) => (
                      <li key={idx} style={{ marginBottom: '4px' }}>{str}</li>
                    ))}
                  </ul>
                </div>
              )}

              {/* Gaps */}
              {analysis.gaps && analysis.gaps.length > 0 && (
                <div>
                  <h4 style={{ fontSize: '13px', fontWeight: 600, color: 'var(--status-warning-text)', marginBottom: 'var(--space-2)', display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>warning</span>
                    Gaps & Areas to Improve
                  </h4>
                  <ul style={{ paddingLeft: 'var(--space-4)', fontSize: '13px', color: 'var(--text-secondary)' }}>
                    {analysis.gaps.map((gap, idx) => (
                      <li key={idx} style={{ marginBottom: '4px' }}>{gap}</li>
                    ))}
                  </ul>
                </div>
              )}

              {/* Suggestions */}
              {analysis.suggestions && analysis.suggestions.length > 0 && (
                <div>
                  <h4 style={{ fontSize: '13px', fontWeight: 600, color: 'var(--accent-primary)', marginBottom: 'var(--space-2)', display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>lightbulb</span>
                    Actionable Formatting Suggestions
                  </h4>
                  <ul style={{ paddingLeft: 'var(--space-4)', fontSize: '13px', color: 'var(--text-secondary)' }}>
                    {analysis.suggestions.map((sug, idx) => (
                      <li key={idx} style={{ marginBottom: '4px' }}>{sug}</li>
                    ))}
                  </ul>
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
