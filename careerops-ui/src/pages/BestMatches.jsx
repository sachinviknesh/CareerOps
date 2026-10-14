import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import { StatusBadge } from '../components/StatusBadge';
import { EmptyState } from '../components/EmptyState';

export function BestMatches({ onNavigate, onSelectOpportunity }) {
  const [matches, setMatches] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Action states
  const [refreshing, setRefreshing] = useState(false);
  const [recalculating, setRecalculating] = useState(false);
  const [actionSuccess, setActionSuccess] = useState(null);
  const [trackingMap, setTrackingMap] = useState({});
  const [expandedMatchId, setExpandedMatchId] = useState(null);

  // Filters
  const [minScore, setMinScore] = useState(0);
  const [selectedType, setSelectedType] = useState('');
  const [selectedWorkMode, setSelectedWorkMode] = useState('');
  const [locationSearch, setLocationSearch] = useState('');
  const [skillFilter, setSkillFilter] = useState('');
  const [sortBy, setSortBy] = useState('fitScore_desc');

  useEffect(() => {
    loadMatches();
  }, []);

  const loadMatches = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await api.getMatches({
        size: 50,
        minScore: minScore > 0 ? minScore : undefined,
        type: selectedType || undefined,
        workMode: selectedWorkMode || undefined,
        location: locationSearch.trim() || undefined,
        skill: skillFilter.trim() || undefined
      });
      const items = data && data.content ? data.content : (Array.isArray(data) ? data : []);
      setMatches(items);
    } catch (err) {
      setError(err.message || 'Failed to load job matches.');
    } finally {
      setLoading(false);
    }
  };

  const handleRefreshJobs = async () => {
    setRefreshing(true);
    setActionSuccess(null);
    setError(null);
    try {
      const result = await api.importJobs({ recalculateMatches: true });
      setActionSuccess(`Job discovery complete! Processed ${result.totalFetched || 0} opportunity feeds (${result.importedCount || 0} newly saved, ${result.duplicateCount || 0} duplicates skipped, ${result.matchedCount || 0} candidate matches computed).`);
      await loadMatches();
    } catch (err) {
      setError(err.message || 'Failed to refresh job feeds.');
    } finally {
      setRefreshing(false);
    }
  };

  const handleRecalculateMatches = async () => {
    setRecalculating(true);
    setActionSuccess(null);
    setError(null);
    try {
      const updated = await api.recalculateMatches();
      const items = updated && updated.content ? updated.content : (Array.isArray(updated) ? updated : []);
      setActionSuccess(`Recalculated fit scores against your profile & resume! Updated ${items.length} opportunities.`);
      setMatches(items);
    } catch (err) {
      setError(err.message || 'Failed to recalculate matches.');
    } finally {
      setRecalculating(false);
    }
  };

  const handleAddToTracker = async (opportunityId, status = 'TO_APPLY') => {
    setTrackingMap(prev => ({ ...prev, [opportunityId]: { loading: true } }));
    try {
      await api.createApplication({
        opportunityId: Number(opportunityId),
        status,
        notes: `Added from Best Matches discovery.`
      });
      setTrackingMap(prev => ({
        ...prev,
        [opportunityId]: { loading: false, tracked: true, msg: 'Tracked!' }
      }));
    } catch (err) {
      setTrackingMap(prev => ({
        ...prev,
        [opportunityId]: { loading: false, tracked: false, error: err.message || 'Already tracked' }
      }));
    }
  };

  // Filter and sort matches
  const filteredMatches = matches.filter(m => {
    if (minScore > 0 && m.fitScore < minScore) return false;
    if (selectedType && m.type !== selectedType) return false;
    if (selectedWorkMode && m.workMode && !m.workMode.toLowerCase().includes(selectedWorkMode.toLowerCase())) return false;
    if (locationSearch && !((m.location || '').toLowerCase().includes(locationSearch.toLowerCase().trim()))) return false;
    if (skillFilter) {
      const kw = skillFilter.toLowerCase().trim();
      const inMatched = (m.matchedSkills || []).some(s => s.toLowerCase().includes(kw));
      const inMissing = (m.missingSkills || []).some(s => s.toLowerCase().includes(kw));
      if (!inMatched && !inMissing) return false;
    }
    return true;
  }).sort((a, b) => {
    if (sortBy === 'fitScore_desc') return b.fitScore - a.fitScore;
    if (sortBy === 'fitScore_asc') return a.fitScore - b.fitScore;
    if (sortBy === 'deadline') {
      const d1 = a.deadline || '9999-12-31';
      const d2 = b.deadline || '9999-12-31';
      return d1.localeCompare(d2);
    }
    return 0;
  });

  const getScoreBadgeClass = (score) => {
    if (score >= 80) return 'badge-success';
    if (score >= 60) return 'badge-warning';
    return 'badge-neutral';
  };

  return (
    <div>
      {/* Header */}
      <div style={{ marginBottom: 'var(--space-6)', display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: 'var(--space-3)' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)' }}>
            <h2 style={{ fontSize: '22px', fontWeight: 600 }}>Best Job Matches</h2>
            <span className="mono badge badge-primary" style={{ fontSize: '11px' }}>
              Auto Discovery
            </span>
          </div>
          <p style={{ fontSize: '13px', color: 'var(--text-secondary)', marginTop: 'var(--space-1)' }}>
            Opportunities scored against your candidate profile, target roles, experience, and uploaded resume skills.
          </p>
        </div>

        <div style={{ display: 'flex', gap: 'var(--space-2)', flexWrap: 'wrap' }}>
          <button
            className="btn-secondary btn-sm"
            onClick={handleRecalculateMatches}
            disabled={recalculating || refreshing}
            title="Recalculate fit against your latest profile and resume changes"
          >
            <span className="material-symbols-outlined" style={{ fontSize: '16px' }}>
              {recalculating ? 'progress_activity' : 'tune'}
            </span>
            {recalculating ? 'Recalculating...' : 'Recalculate Fit'}
          </button>

          <button
            className="btn-primary btn-sm"
            onClick={handleRefreshJobs}
            disabled={refreshing || recalculating}
            title="Poll configured job feeds and automatically match new listings"
          >
            <span className="material-symbols-outlined" style={{ fontSize: '16px' }}>
              {refreshing ? 'autorenew' : 'sync'}
            </span>
            {refreshing ? 'Discovering Jobs...' : 'Refresh Jobs'}
          </button>
        </div>
      </div>

      {/* Notifications */}
      {actionSuccess && (
        <div className="alert alert-success" style={{ marginBottom: 'var(--space-4)' }}>
          <span className="material-symbols-outlined">check_circle</span>
          <div>{actionSuccess}</div>
        </div>
      )}

      {error && (
        <div className="alert alert-danger" style={{ marginBottom: 'var(--space-4)' }}>
          <span className="material-symbols-outlined">error</span>
          <div>{error}</div>
        </div>
      )}

      {/* Search & Filter Bar */}
      <div style={{
        backgroundColor: 'var(--bg-surface)',
        border: '1px solid var(--border-subtle)',
        borderRadius: 'var(--radius-md)',
        padding: 'var(--space-4)',
        marginBottom: 'var(--space-6)',
        boxShadow: 'var(--shadow-subtle)',
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(170px, 1fr))',
        gap: 'var(--space-3)',
        alignItems: 'end'
      }}>
        {/* Min Fit Score */}
        <div>
          <label style={{ display: 'block', fontSize: '11px', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '4px' }}>
            Min Fit Score
          </label>
          <select
            value={minScore}
            onChange={(e) => setMinScore(Number(e.target.value))}
            style={{ width: '100%', padding: '6px 10px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-subtle)', backgroundColor: 'var(--bg-app)', fontSize: '13px' }}
          >
            <option value={0}>All Scores (0%+)</option>
            <option value={50}>Moderate (50%+)</option>
            <option value={70}>Good Match (70%+)</option>
            <option value={80}>High Match (80%+)</option>
            <option value={90}>Exceptional (90%+)</option>
          </select>
        </div>

        {/* Opportunity Type */}
        <div>
          <label style={{ display: 'block', fontSize: '11px', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '4px' }}>
            Opportunity Type
          </label>
          <select
            value={selectedType}
            onChange={(e) => setSelectedType(e.target.value)}
            style={{ width: '100%', padding: '6px 10px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-subtle)', backgroundColor: 'var(--bg-app)', fontSize: '13px' }}
          >
            <option value="">All Types</option>
            <option value="JOB">Job</option>
            <option value="INTERNSHIP">Internship</option>
            <option value="HACKATHON">Hackathon</option>
            <option value="CTF">CTF Competition</option>
            <option value="FELLOWSHIP">Fellowship</option>
          </select>
        </div>

        {/* Work Mode */}
        <div>
          <label style={{ display: 'block', fontSize: '11px', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '4px' }}>
            Work Mode
          </label>
          <select
            value={selectedWorkMode}
            onChange={(e) => setSelectedWorkMode(e.target.value)}
            style={{ width: '100%', padding: '6px 10px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-subtle)', backgroundColor: 'var(--bg-app)', fontSize: '13px' }}
          >
            <option value="">All Modes</option>
            <option value="Remote">Remote</option>
            <option value="Hybrid">Hybrid</option>
            <option value="On-site">On-site</option>
          </select>
        </div>

        {/* Location search */}
        <div>
          <label style={{ display: 'block', fontSize: '11px', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '4px' }}>
            Location
          </label>
          <input
            type="text"
            placeholder="e.g. San Francisco, Remote"
            value={locationSearch}
            onChange={(e) => setLocationSearch(e.target.value)}
            style={{ width: '100%', padding: '6px 10px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-subtle)', backgroundColor: 'var(--bg-app)', fontSize: '13px' }}
          />
        </div>

        {/* Skill filter */}
        <div>
          <label style={{ display: 'block', fontSize: '11px', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '4px' }}>
            Filter by Skill
          </label>
          <input
            type="text"
            placeholder="e.g. Java, Docker, SIEM"
            value={skillFilter}
            onChange={(e) => setSkillFilter(e.target.value)}
            style={{ width: '100%', padding: '6px 10px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-subtle)', backgroundColor: 'var(--bg-app)', fontSize: '13px' }}
          />
        </div>

        {/* Sort By */}
        <div>
          <label style={{ display: 'block', fontSize: '11px', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '4px' }}>
            Sort Order
          </label>
          <select
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value)}
            style={{ width: '100%', padding: '6px 10px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-subtle)', backgroundColor: 'var(--bg-app)', fontSize: '13px' }}
          >
            <option value="fitScore_desc">Highest Fit Score</option>
            <option value="fitScore_asc">Lowest Fit Score</option>
            <option value="deadline">Earliest Deadline</option>
          </select>
        </div>
      </div>

      {/* Loading state */}
      {loading ? (
        <div style={{ padding: 'var(--space-8)', textAlign: 'center', color: 'var(--text-secondary)' }}>
          <span className="material-symbols-outlined" style={{ fontSize: '32px', animation: 'spin 1s linear infinite' }}>
            autorenew
          </span>
          <p style={{ marginTop: 'var(--space-2)', fontSize: '14px' }}>Evaluating candidate profile and calculating fit matches...</p>
        </div>
      ) : filteredMatches.length === 0 ? (
        <div style={{
          backgroundColor: 'var(--bg-surface)',
          border: '1px solid var(--border-subtle)',
          borderRadius: 'var(--radius-lg)',
          padding: 'var(--space-8)',
          textAlign: 'center'
        }}>
          <EmptyState
            icon="auto_awesome"
            title="No Matching Opportunities Found"
            description={matches.length === 0 
              ? "Click 'Refresh Jobs' above to import and automatically calculate fit scores for curated opportunities." 
              : "No jobs match your selected filters. Try lowering the minimum fit score or clearing search keywords."
            }
            actionText={matches.length === 0 ? "Discover & Match Jobs" : "Reset Filters"}
            onAction={matches.length === 0 ? handleRefreshJobs : () => {
              setMinScore(0);
              setSelectedType('');
              setSelectedWorkMode('');
              setLocationSearch('');
              setSkillFilter('');
            }}
          />
        </div>
      ) : (
        /* Match Cards Grid */
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '12px', color: 'var(--text-secondary)' }}>
            <span>Showing {filteredMatches.length} matching {filteredMatches.length === 1 ? 'opportunity' : 'opportunities'}</span>
            <span className="mono">Sorted by: {sortBy === 'fitScore_desc' ? 'Fit Score (High to Low)' : sortBy}</span>
          </div>

          {filteredMatches.map((m) => {
            const oppId = m.opportunityId;
            const isExpanded = expandedMatchId === (m.matchId || oppId);
            const trackingState = trackingMap[oppId] || {};

            return (
              <div
                key={m.matchId || oppId}
                style={{
                  backgroundColor: 'var(--bg-surface)',
                  border: '1px solid var(--border-subtle)',
                  borderRadius: 'var(--radius-lg)',
                  padding: 'var(--space-5)',
                  boxShadow: 'var(--shadow-card)',
                  display: 'flex',
                  flexDirection: 'column',
                  gap: 'var(--space-4)',
                  transition: 'border-color 0.15s ease'
                }}
              >
                {/* Top Row: Title, Org, Tags, Fit Score Badge */}
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 'var(--space-4)', flexWrap: 'wrap' }}>
                  <div style={{ flex: 1, minWidth: '260px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-2)', marginBottom: 'var(--space-1)', flexWrap: 'wrap' }}>
                      <StatusBadge type={m.type || 'JOB'} />
                      {m.workMode && (
                        <span className="badge badge-neutral" style={{ fontSize: '11px' }}>
                          {m.workMode}
                        </span>
                      )}
                      {m.roleFamily && (
                        <span className="badge badge-info" style={{ fontSize: '11px' }}>
                          {m.roleFamily}
                        </span>
                      )}
                    </div>

                    <h3
                      onClick={() => {
                        if (onSelectOpportunity) onSelectOpportunity(oppId);
                        onNavigate('opportunityDetail', oppId);
                      }}
                      style={{
                        fontSize: '18px',
                        fontWeight: 600,
                        color: 'var(--text-primary)',
                        cursor: 'pointer',
                        textDecoration: 'none'
                      }}
                      onMouseEnter={(e) => e.target.style.color = 'var(--accent-primary)'}
                      onMouseLeave={(e) => e.target.style.color = 'var(--text-primary)'}
                    >
                      {m.title}
                    </h3>

                    <div style={{ fontSize: '13px', color: 'var(--text-secondary)', display: 'flex', alignItems: 'center', gap: 'var(--space-3)', marginTop: '4px', flexWrap: 'wrap' }}>
                      <span style={{ fontWeight: 500, color: 'var(--text-primary)' }}>
                        {m.organization || 'Independent Organization'}
                      </span>
                      {m.location && (
                        <span>• {m.location}</span>
                      )}
                      {m.deadline && (
                        <span>• Deadline: <strong className="mono" style={{ color: 'var(--text-primary)' }}>{m.deadline}</strong></span>
                      )}
                    </div>
                  </div>

                  {/* Fit Score Gauge Card */}
                  <div style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: 'var(--space-3)',
                    backgroundColor: 'var(--bg-surface-subtle)',
                    padding: 'var(--space-2) var(--space-4)',
                    borderRadius: 'var(--radius-md)',
                    border: '1px solid var(--border-subtle)'
                  }}>
                    <div style={{ textAlign: 'right' }}>
                      <div style={{ fontSize: '11px', color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '0.05em', fontWeight: 600 }}>
                        Candidate Fit
                      </div>
                      <div style={{ fontSize: '11px', color: 'var(--text-tertiary)' }} className="mono">
                        {m.experienceMatch && m.experienceMatch !== 'UNKNOWN' ? `Exp: ${m.experienceMatch}` : 'Profile Evaluated'}
                      </div>
                    </div>
                    <div
                      className={`mono badge ${getScoreBadgeClass(m.fitScore)}`}
                      style={{
                        fontSize: '20px',
                        fontWeight: 700,
                        padding: '6px 12px',
                        borderRadius: 'var(--radius-md)'
                      }}
                    >
                      {m.fitScore}%
                    </div>
                  </div>
                </div>

                {/* Middle Row: Matched Skills and Missing Skills */}
                <div style={{
                  display: 'grid',
                  gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))',
                  gap: 'var(--space-3)',
                  backgroundColor: 'var(--bg-app)',
                  padding: 'var(--space-3) var(--space-4)',
                  borderRadius: 'var(--radius-md)'
                }}>
                  {/* Matched Skills */}
                  <div>
                    <div style={{ fontSize: '11px', fontWeight: 600, color: 'var(--status-success-text)', marginBottom: '6px', display: 'flex', alignItems: 'center', gap: '4px' }}>
                      <span className="material-symbols-outlined" style={{ fontSize: '14px' }}>check_circle</span>
                      Matched Skills ({m.matchedSkills?.length || 0})
                    </div>
                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: '4px' }}>
                      {m.matchedSkills && m.matchedSkills.length > 0 ? (
                        m.matchedSkills.map((s, idx) => (
                          <span
                            key={idx}
                            className="badge badge-success"
                            style={{ fontSize: '11px', padding: '2px 8px' }}
                          >
                            {s}
                          </span>
                        ))
                      ) : (
                        <span style={{ fontSize: '12px', color: 'var(--text-tertiary)' }}>No exact profile skill match</span>
                      )}
                    </div>
                  </div>

                  {/* Missing Skills */}
                  <div>
                    <div style={{ fontSize: '11px', fontWeight: 600, color: 'var(--status-warning-text)', marginBottom: '6px', display: 'flex', alignItems: 'center', gap: '4px' }}>
                      <span className="material-symbols-outlined" style={{ fontSize: '14px' }}>warning</span>
                      Skill Gaps to Close ({m.missingSkills?.length || 0})
                    </div>
                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: '4px' }}>
                      {m.missingSkills && m.missingSkills.length > 0 ? (
                        m.missingSkills.map((s, idx) => (
                          <span
                            key={idx}
                            className="badge badge-warning"
                            style={{ fontSize: '11px', padding: '2px 8px' }}
                          >
                            {s}
                          </span>
                        ))
                      ) : (
                        <span className="badge badge-success" style={{ fontSize: '11px' }}>All required skills covered!</span>
                      )}
                    </div>
                  </div>
                </div>

                {/* Expandable Preparation Recommendations */}
                {isExpanded && (
                  <div style={{
                    padding: 'var(--space-4)',
                    backgroundColor: 'var(--bg-surface-subtle)',
                    borderRadius: 'var(--radius-md)',
                    border: '1px solid var(--border-subtle)',
                    fontSize: '13px'
                  }}>
                    <div style={{ fontWeight: 600, color: 'var(--text-primary)', marginBottom: 'var(--space-2)', display: 'flex', alignItems: 'center', gap: '6px' }}>
                      <span className="material-symbols-outlined" style={{ fontSize: '18px', color: 'var(--accent-primary)' }}>lightbulb</span>
                      Targeted Preparation Strategy
                    </div>
                    <div style={{ color: 'var(--text-secondary)', lineHeight: 1.6, whiteSpace: 'pre-line' }}>
                      {m.recommendations && m.recommendations.length > 0
                        ? (Array.isArray(m.recommendations) ? m.recommendations.join('\n• ') : m.recommendations)
                        : 'Highlight your experience in related core competencies, prepare hands-on code or architecture walkthroughs, and practice scenario-based problem solving.'}
                    </div>
                  </div>
                )}

                {/* Bottom Row: Actions */}
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', paddingTop: 'var(--space-2)', borderTop: '1px solid var(--border-subtle)', flexWrap: 'wrap', gap: 'var(--space-2)' }}>
                  <button
                    className="btn-secondary btn-sm"
                    onClick={() => setExpandedMatchId(isExpanded ? null : (m.matchId || oppId))}
                    style={{ fontSize: '12px' }}
                  >
                    <span className="material-symbols-outlined" style={{ fontSize: '16px' }}>
                      {isExpanded ? 'expand_less' : 'expand_more'}
                    </span>
                    {isExpanded ? 'Hide Recommendations' : 'Preparation Recommendations'}
                  </button>

                  <div style={{ display: 'flex', gap: 'var(--space-2)', alignItems: 'center' }}>
                    <button
                      className="btn-secondary btn-sm"
                      onClick={() => {
                        if (onSelectOpportunity) onSelectOpportunity(oppId);
                        onNavigate('opportunityDetail', oppId);
                      }}
                    >
                      <span className="material-symbols-outlined" style={{ fontSize: '16px' }}>visibility</span>
                      View Details & Fit Analysis
                    </button>

                    {trackingState.tracked ? (
                      <span className="badge badge-success" style={{ display: 'flex', alignItems: 'center', gap: '4px', padding: '6px 10px' }}>
                        <span className="material-symbols-outlined" style={{ fontSize: '16px' }}>check</span>
                        Added to Tracker
                      </span>
                    ) : (
                      <button
                        className="btn-primary btn-sm"
                        disabled={trackingState.loading}
                        onClick={() => handleAddToTracker(oppId)}
                      >
                        <span className="material-symbols-outlined" style={{ fontSize: '16px' }}>
                          {trackingState.loading ? 'progress_activity' : 'playlist_add'}
                        </span>
                        {trackingState.loading ? 'Adding...' : 'Add to Tracker'}
                      </button>
                    )}
                  </div>
                </div>

                {trackingState.error && (
                  <div style={{ fontSize: '12px', color: 'var(--status-danger-text)' }}>
                    {trackingState.error}
                  </div>
                )}
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}

export default BestMatches;
