import React from 'react';

export function FitPanel({ fitData, loading = false, error = null, onAiReview = null, aiLoading = false, aiReview = null }) {
  if (loading) {
    return (
      <div style={{ padding: 'var(--space-6)', textAlign: 'center', color: 'var(--text-secondary)' }}>
        <span className="material-symbols-outlined" style={{ animation: 'spin 1s infinite linear', fontSize: '28px', marginBottom: 'var(--space-2)' }}>
          autorenew
        </span>
        <div>Calculating multi-factor job fit...</div>
        <div style={{ fontSize: '12px', color: 'var(--text-tertiary)', marginTop: '4px' }}>
          Comparing skills, resume evidence, and role requirements
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="alert alert-danger">
        <span className="material-symbols-outlined">error</span>
        <div>{error}</div>
      </div>
    );
  }

  if (!fitData) {
    return (
      <div style={{ padding: 'var(--space-6)', textAlign: 'center', color: 'var(--text-tertiary)' }}>
        Click "Analyze Job Fit" to calculate match heuristics against your profile and uploaded resume.
      </div>
    );
  }

  const { score, confidence, matchedSkills = [], missingSkills = [], evidenceFound = [], eligibilityChecks = [], resumeSignals = [], nextActions = [], disclaimer } = fitData;

  const getScoreColor = (s) => {
    if (s >= 70) return 'var(--status-success-text)';
    if (s >= 45) return 'var(--status-warning-text)';
    return 'var(--status-danger-text)';
  };

  const getScoreBg = (s) => {
    if (s >= 70) return 'var(--status-success-bg)';
    if (s >= 45) return 'var(--status-warning-bg)';
    return 'var(--status-danger-bg)';
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-5)' }}>
      {/* Top Score Banner */}
      <div style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        padding: 'var(--space-4)',
        backgroundColor: getScoreBg(score),
        borderRadius: 'var(--radius-md)',
        border: '1px solid var(--border-subtle)'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-4)' }}>
          <div style={{
            fontSize: '36px',
            fontWeight: 700,
            color: getScoreColor(score),
            lineHeight: 1
          }} className="mono">
            {score}%
          </div>
          <div>
            <div style={{ fontSize: '15px', fontWeight: 600, color: 'var(--text-primary)' }}>
              Overall Match Alignment
            </div>
            <div style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>
              Confidence level: <span className="mono" style={{ fontWeight: 600 }}>{confidence}</span>
            </div>
          </div>
        </div>

        {onAiReview && (
          <button
            className="btn-secondary btn-sm"
            onClick={onAiReview}
            disabled={aiLoading}
            title="Request grounded AI enhancement using configured LLM"
          >
            <span className="material-symbols-outlined" style={{ fontSize: '16px' }}>psychology</span>
            {aiLoading ? 'Reviewing...' : 'AI In-Depth Review'}
          </button>
        )}
      </div>

      {/* Mandatory Disclaimer */}
      {disclaimer && (
        <div style={{
          fontSize: '11px',
          color: 'var(--text-secondary)',
          backgroundColor: 'var(--bg-surface-subtle)',
          padding: 'var(--space-2) var(--space-3)',
          borderRadius: 'var(--radius-sm)',
          borderLeft: '3px solid var(--accent-primary)'
        }}>
          <strong>Note:</strong> {disclaimer}
        </div>
      )}

      {/* Skills Comparison */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: 'var(--space-4)' }}>
        <div style={{
          backgroundColor: 'var(--bg-surface)',
          padding: 'var(--space-4)',
          borderRadius: 'var(--radius-md)',
          border: '1px solid var(--border-subtle)'
        }}>
          <h4 style={{ fontSize: '13px', fontWeight: 600, color: 'var(--status-success-text)', marginBottom: 'var(--space-3)', display: 'flex', alignItems: 'center', gap: '6px' }}>
            <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>check_circle</span>
            Matched Skills ({matchedSkills.length})
          </h4>
          {matchedSkills.length === 0 ? (
            <div style={{ fontSize: '12px', color: 'var(--text-tertiary)' }}>No required skills matched in profile.</div>
          ) : (
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px' }}>
              {matchedSkills.map((s, idx) => (
                <span key={idx} className="badge badge-success">{s}</span>
              ))}
            </div>
          )}
        </div>

        <div style={{
          backgroundColor: 'var(--bg-surface)',
          padding: 'var(--space-4)',
          borderRadius: 'var(--radius-md)',
          border: '1px solid var(--border-subtle)'
        }}>
          <h4 style={{ fontSize: '13px', fontWeight: 600, color: 'var(--status-danger-text)', marginBottom: 'var(--space-3)', display: 'flex', alignItems: 'center', gap: '6px' }}>
            <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>warning</span>
            Missing Required Skills ({missingSkills.length})
          </h4>
          {missingSkills.length === 0 ? (
            <div style={{ fontSize: '12px', color: 'var(--status-success-text)' }}>All required skills found!</div>
          ) : (
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px' }}>
              {missingSkills.map((s, idx) => (
                <span key={idx} className="badge badge-danger">{s}</span>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Eligibility Checks */}
      {eligibilityChecks && eligibilityChecks.length > 0 && (
        <div style={{
          backgroundColor: 'var(--bg-surface)',
          padding: 'var(--space-4)',
          borderRadius: 'var(--radius-md)',
          border: '1px solid var(--border-subtle)'
        }}>
          <h4 style={{ fontSize: '13px', fontWeight: 600, marginBottom: 'var(--space-3)' }}>
            Eligibility & Role Prerequisites
          </h4>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-2)' }}>
            {eligibilityChecks.map((chk, idx) => (
              <div key={idx} style={{
                display: 'flex',
                alignItems: 'flex-start',
                gap: 'var(--space-2)',
                fontSize: '12px',
                padding: 'var(--space-2)',
                borderRadius: 'var(--radius-sm)',
                backgroundColor: 'var(--bg-surface-subtle)'
              }}>
                <span className={`badge ${chk.result === 'MATCH' ? 'badge-success' : chk.result === 'PARTIAL' ? 'badge-warning' : 'badge-neutral'}`}>
                  {chk.criterion}: {chk.result}
                </span>
                <span style={{ color: 'var(--text-secondary)', flex: 1 }}>
                  {chk.detail}
                </span>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Evidence & Resume Signals */}
      {evidenceFound && evidenceFound.length > 0 && (
        <div style={{
          backgroundColor: 'var(--bg-surface)',
          padding: 'var(--space-4)',
          borderRadius: 'var(--radius-md)',
          border: '1px solid var(--border-subtle)'
        }}>
          <h4 style={{ fontSize: '13px', fontWeight: 600, marginBottom: 'var(--space-2)' }}>
            Extracted Resume Evidence
          </h4>
          <ul style={{ paddingLeft: 'var(--space-4)', fontSize: '12px', color: 'var(--text-secondary)' }}>
            {evidenceFound.map((ev, idx) => (
              <li key={idx} style={{ marginBottom: '4px' }}>{ev}</li>
            ))}
          </ul>
        </div>
      )}

      {/* Next Actions */}
      {nextActions && nextActions.length > 0 && (
        <div style={{
          backgroundColor: 'var(--bg-surface)',
          padding: 'var(--space-4)',
          borderRadius: 'var(--radius-md)',
          border: '1px solid var(--border-subtle)'
        }}>
          <h4 style={{ fontSize: '13px', fontWeight: 600, color: 'var(--accent-primary)', marginBottom: 'var(--space-2)' }}>
            Recommended Preparation Steps
          </h4>
          <ul style={{ paddingLeft: 'var(--space-4)', fontSize: '12px', color: 'var(--text-secondary)' }}>
            {nextActions.map((act, idx) => (
              <li key={idx} style={{ marginBottom: '4px' }}>{act}</li>
            ))}
          </ul>
        </div>
      )}

      {/* AI Review Results if available */}
      {aiReview && (
        <div style={{
          backgroundColor: 'var(--bg-surface)',
          padding: 'var(--space-5)',
          borderRadius: 'var(--radius-md)',
          border: '2px solid var(--accent-border)'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)', marginBottom: 'var(--space-3)' }}>
            <span className="material-symbols-outlined" style={{ color: 'var(--accent-primary)' }}>psychology</span>
            <h4 style={{ fontSize: '15px', fontWeight: 600 }}>Grounded AI Assessment</h4>
          </div>

          <p style={{ fontSize: '13px', color: 'var(--text-primary)', marginBottom: 'var(--space-4)', lineHeight: 1.6 }}>
            {aiReview.summary}
          </p>

          {aiReview.strengths && aiReview.strengths.length > 0 && (
            <div style={{ marginBottom: 'var(--space-3)' }}>
              <div style={{ fontSize: '12px', fontWeight: 600, color: 'var(--status-success-text)', marginBottom: '4px' }}>
                Key Strengths
              </div>
              <ul style={{ paddingLeft: 'var(--space-4)', fontSize: '12px', color: 'var(--text-secondary)' }}>
                {aiReview.strengths.map((s, idx) => <li key={idx}>{s}</li>)}
              </ul>
            </div>
          )}

          {aiReview.evidenceGaps && aiReview.evidenceGaps.length > 0 && (
            <div style={{ marginBottom: 'var(--space-3)' }}>
              <div style={{ fontSize: '12px', fontWeight: 600, color: 'var(--status-warning-text)', marginBottom: '4px' }}>
                Evidence Gaps & Missing Validation
              </div>
              <ul style={{ paddingLeft: 'var(--space-4)', fontSize: '12px', color: 'var(--text-secondary)' }}>
                {aiReview.evidenceGaps.map((g, idx) => <li key={idx}>{g}</li>)}
              </ul>
            </div>
          )}

          {aiReview.interviewFocus && aiReview.interviewFocus.length > 0 && (
            <div style={{ marginBottom: 'var(--space-3)' }}>
              <div style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-primary)', marginBottom: '4px' }}>
                Target Interview Questions & Focus Areas
              </div>
              <ul style={{ paddingLeft: 'var(--space-4)', fontSize: '12px', color: 'var(--text-secondary)' }}>
                {aiReview.interviewFocus.map((f, idx) => <li key={idx}>{f}</li>)}
              </ul>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
