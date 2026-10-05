import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import { StatusBadge } from '../components/StatusBadge';
import { EmptyState } from '../components/EmptyState';

export function Dashboard({ onNavigate, onSelectOpportunity }) {
  const [stats, setStats] = useState({
    toApply: 0,
    applied: 0,
    inProgress: 0,
    interview: 0,
    selected: 0,
    rejected: 0,
    totalTracked: 0
  });
  const [closingSoon, setClosingSoon] = useState([]);
  const [recentApplications, setRecentApplications] = useState([]);
  const [recentOpportunities, setRecentOpportunities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [statsData, closingData, appsData, oppsData] = await Promise.all([
        api.getApplicationStats().catch(() => ({})),
        api.getClosingSoon(14).catch(() => []),
        api.getApplications({ page: 0, size: 5, sort: 'createdAt,desc' }).catch(() => ({ content: [] })),
        api.getOpportunities({ page: 0, size: 5, sort: 'createdAt,desc' }).catch(() => ({ content: [] }))
      ]);

      if (statsData) setStats(statsData);
      if (closingData) setClosingSoon(closingData);
      if (appsData && appsData.content) setRecentApplications(appsData.content);
      if (oppsData && oppsData.content) setRecentOpportunities(oppsData.content);
    } catch (err) {
      setError('Failed to load dashboard data.');
    } finally {
      setLoading(false);
    }
  };

  const statItems = [
    { label: 'To Apply', value: stats.toApply || 0, badge: 'badge-warning', icon: 'bookmark' },
    { label: 'Applied', value: stats.applied || 0, badge: 'badge-neutral', icon: 'send' },
    { label: 'In Progress', value: stats.inProgress || 0, badge: 'badge-info', icon: 'hourglass_top' },
    { label: 'Interview', value: stats.interview || 0, badge: 'badge-info', icon: 'forum' },
    { label: 'Selected', value: stats.selected || 0, badge: 'badge-success', icon: 'check_circle' },
    { label: 'Closing Soon', value: closingSoon.length, badge: 'badge-danger', icon: 'notification_important' }
  ];

  return (
    <div>
      {/* Header */}
      <div style={{ marginBottom: 'var(--space-6)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h2 style={{ fontSize: '22px', fontWeight: 600 }}>Operational Console</h2>
          <p style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>
            Pipeline tracking, impending deadlines, and active opportunity feed
          </p>
        </div>
        <div style={{ display: 'flex', gap: 'var(--space-2)' }}>
          <button className="btn-secondary btn-sm" onClick={loadDashboardData}>
            <span className="material-symbols-outlined" style={{ fontSize: '16px' }}>refresh</span>
            Refresh
          </button>
          <button className="btn-primary btn-sm" onClick={() => onNavigate('opportunities')}>
            <span className="material-symbols-outlined" style={{ fontSize: '16px' }}>search</span>
            Browse Opportunities
          </button>
        </div>
      </div>

      {error && (
        <div className="alert alert-danger">
          <span className="material-symbols-outlined">error</span>
          <div>{error}</div>
        </div>
      )}

      {/* Compact Operational Metric Strip */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))',
        gap: 'var(--space-3)',
        marginBottom: 'var(--space-6)'
      }}>
        {statItems.map((item, idx) => (
          <div
            key={idx}
            style={{
              backgroundColor: 'var(--bg-surface)',
              border: '1px solid var(--border-subtle)',
              borderRadius: 'var(--radius-md)',
              padding: 'var(--space-3) var(--space-4)',
              boxShadow: 'var(--shadow-subtle)',
              display: 'flex',
              flexDirection: 'column',
              justifyContent: 'space-between'
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', color: 'var(--text-secondary)', marginBottom: 'var(--space-1)' }}>
              <span style={{ fontSize: '12px', fontWeight: 500 }}>{item.label}</span>
              <span className="material-symbols-outlined" style={{ fontSize: '16px', color: 'var(--text-tertiary)' }}>{item.icon}</span>
            </div>
            <div style={{ fontSize: '24px', fontWeight: 700, color: 'var(--text-primary)' }} className="mono">
              {item.value}
            </div>
          </div>
        ))}
      </div>

      {/* Two Column Layout: Deadlines / Recent Applications & Recent Opportunities */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(400px, 1fr))', gap: 'var(--space-5)' }}>
        {/* Left Column: Deadlines & Tracker Activity */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-5)' }}>
          {/* Closing Soon */}
          <div style={{
            backgroundColor: 'var(--bg-surface)',
            border: '1px solid var(--border-subtle)',
            borderRadius: 'var(--radius-md)',
            overflow: 'hidden',
            boxShadow: 'var(--shadow-subtle)'
          }}>
            <div style={{
              padding: 'var(--space-3) var(--space-4)',
              borderBottom: '1px solid var(--border-subtle)',
              backgroundColor: 'var(--bg-surface-subtle)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between'
            }}>
              <span style={{ fontSize: '13px', fontWeight: 600 }}>Impending Deadlines (&lt; 14 days)</span>
              <span className="mono badge badge-warning">{closingSoon.length} urgent</span>
            </div>
            {closingSoon.length === 0 ? (
              <div style={{ padding: 'var(--space-5)', textAlign: 'center', fontSize: '13px', color: 'var(--text-secondary)' }}>
                No active opportunities closing in the next 14 days.
              </div>
            ) : (
              <table>
                <thead>
                  <tr>
                    <th>Opportunity</th>
                    <th>Org</th>
                    <th>Deadline</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {closingSoon.map((opp) => (
                    <tr key={opp.id}>
                      <td style={{ fontWeight: 500 }}>{opp.title}</td>
                      <td style={{ color: 'var(--text-secondary)' }}>{opp.organization || '—'}</td>
                      <td className="mono" style={{ color: 'var(--status-danger-text)', fontWeight: 600, fontSize: '12px' }}>
                        {opp.deadline}
                      </td>
                      <td>
                        <button
                          className="btn-secondary btn-sm"
                          onClick={() => {
                            if (onSelectOpportunity) onSelectOpportunity(opp.id);
                            onNavigate('opportunityDetail');
                          }}
                        >
                          View
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>

          {/* Recent Applications */}
          <div style={{
            backgroundColor: 'var(--bg-surface)',
            border: '1px solid var(--border-subtle)',
            borderRadius: 'var(--radius-md)',
            overflow: 'hidden',
            boxShadow: 'var(--shadow-subtle)'
          }}>
            <div style={{
              padding: 'var(--space-3) var(--space-4)',
              borderBottom: '1px solid var(--border-subtle)',
              backgroundColor: 'var(--bg-surface-subtle)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between'
            }}>
              <span style={{ fontSize: '13px', fontWeight: 600 }}>Recent Tracked Applications</span>
              <button
                className="btn-secondary btn-sm"
                onClick={() => onNavigate('applications')}
                style={{ fontSize: '11px', padding: '2px 8px' }}
              >
                View Tracker
              </button>
            </div>
            {recentApplications.length === 0 ? (
              <div style={{ padding: 'var(--space-5)', textAlign: 'center', fontSize: '13px', color: 'var(--text-secondary)' }}>
                No applications tracked yet. Start browsing opportunities!
              </div>
            ) : (
              <table>
                <thead>
                  <tr>
                    <th>Opportunity</th>
                    <th>Status</th>
                    <th>Next Action</th>
                  </tr>
                </thead>
                <tbody>
                  {recentApplications.map((app) => (
                    <tr key={app.id}>
                      <td style={{ fontWeight: 500 }}>{app.opportunity ? app.opportunity.title : `App #${app.id}`}</td>
                      <td><StatusBadge status={app.status} /></td>
                      <td className="mono" style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>
                        {app.nextActionDate || '—'}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>

        {/* Right Column: Recent Opportunities Feed */}
        <div style={{
          backgroundColor: 'var(--bg-surface)',
          border: '1px solid var(--border-subtle)',
          borderRadius: 'var(--radius-md)',
          overflow: 'hidden',
          boxShadow: 'var(--shadow-subtle)',
          display: 'flex',
          flexDirection: 'column'
        }}>
          <div style={{
            padding: 'var(--space-3) var(--space-4)',
            borderBottom: '1px solid var(--border-subtle)',
            backgroundColor: 'var(--bg-surface-subtle)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between'
          }}>
            <span style={{ fontSize: '13px', fontWeight: 600 }}>Newly Added Opportunities</span>
            <button
              className="btn-secondary btn-sm"
              onClick={() => onNavigate('opportunities')}
              style={{ fontSize: '11px', padding: '2px 8px' }}
            >
              All Opportunities
            </button>
          </div>
          {recentOpportunities.length === 0 ? (
            <div style={{ padding: 'var(--space-6)', textAlign: 'center' }}>
              <EmptyState
                icon="work_outline"
                title="No opportunities posted yet"
                description="Create your first job, internship, or hackathon listing to track."
                actionText="Create Opportunity"
                onAction={() => onNavigate('opportunities')}
              />
            </div>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>Title & Org</th>
                  <th>Type</th>
                  <th>Role Family</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {recentOpportunities.map((opp) => (
                  <tr key={opp.id}>
                    <td>
                      <div style={{ fontWeight: 600 }}>{opp.title}</div>
                      <div style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>
                        {opp.organization || 'Independent'}{opp.location ? ` • ${opp.location}` : ''}
                      </div>
                    </td>
                    <td><StatusBadge type={opp.type} /></td>
                    <td style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>{opp.roleFamily || 'General'}</td>
                    <td>
                      <button
                        className="btn-secondary btn-sm"
                        onClick={() => {
                          if (onSelectOpportunity) onSelectOpportunity(opp.id);
                          onNavigate('opportunityDetail');
                        }}
                      >
                        Fit & Details
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  );
}
