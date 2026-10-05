import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import { DataTable } from '../components/DataTable';
import { Pagination } from '../components/Pagination';
import { StatusBadge } from '../components/StatusBadge';

export function Applications({ onNavigate, onSelectOpportunity }) {
  const [applications, setApplications] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Filters
  const [statusFilter, setStatusFilter] = useState('');
  const [typeFilter, setTypeFilter] = useState('');
  const [sort, setSort] = useState('createdAt,desc');

  // Edit status/notes modal
  const [editingApp, setEditingApp] = useState(null);
  const [editStatus, setEditStatus] = useState('TO_APPLY');
  const [editNotes, setEditNotes] = useState('');
  const [editNextAction, setEditNextAction] = useState('');
  const [editAppliedAt, setEditAppliedAt] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    loadApplications();
  }, [page, statusFilter, typeFilter, sort]);

  const loadApplications = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await api.getApplications({
        page,
        size: 10,
        sort,
        status: statusFilter || undefined,
        type: typeFilter || undefined
      });
      setApplications(res.content || []);
      setTotalPages(res.totalPages || 0);
      setTotalElements(res.totalElements || 0);
    } catch (err) {
      setError(err.message || 'Failed to load applications');
    } finally {
      setLoading(false);
    }
  };

  const handleOpenEdit = (app) => {
    setEditingApp(app);
    setEditStatus(app.status);
    setEditNotes(app.notes || '');
    setEditNextAction(app.nextActionDate || '');
    setEditAppliedAt(app.appliedAt || '');
  };

  const handleSaveEdit = async (e) => {
    e.preventDefault();
    if (!editingApp) return;
    setSaving(true);
    try {
      await api.updateApplication(editingApp.id, {
        status: editStatus,
        notes: editNotes,
        nextActionDate: editNextAction || null,
        appliedAt: editAppliedAt || null
      });
      setEditingApp(null);
      loadApplications();
    } catch (err) {
      alert(err.message || 'Failed to update application');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id, oppTitle) => {
    if (!window.confirm(`Remove "${oppTitle}" from your application tracker?`)) return;
    try {
      await api.deleteApplication(id);
      loadApplications();
    } catch (err) {
      alert(err.message || 'Failed to delete application');
    }
  };

  const columns = [
    {
      header: 'Opportunity & Org',
      width: '30%',
      render: (app) => {
        const opp = app.opportunity || {};
        return (
          <div>
            <div
              style={{ fontWeight: 600, color: 'var(--text-primary)', cursor: 'pointer' }}
              onClick={() => {
                if (opp.id && onSelectOpportunity) onSelectOpportunity(opp.id);
                onNavigate('opportunityDetail');
              }}
            >
              {opp.title || 'Untitled Opportunity'}
            </div>
            <div style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>
              {opp.organization || 'Independent'} {opp.location ? `• ${opp.location}` : ''}
            </div>
          </div>
        );
      }
    },
    {
      header: 'Type',
      width: '12%',
      render: (app) => <StatusBadge type={app.opportunity ? app.opportunity.type : null} />
    },
    {
      header: 'Stage Status',
      width: '15%',
      render: (app) => <StatusBadge status={app.status} />
    },
    {
      header: 'Applied Date',
      width: '12%',
      render: (app) => (
        <span className="mono" style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>
          {app.appliedAt || 'Not yet applied'}
        </span>
      )
    },
    {
      header: 'Next Action',
      width: '15%',
      render: (app) => (
        <span className="mono" style={{ fontSize: '12px', color: app.nextActionDate ? 'var(--status-warning-text)' : 'var(--text-tertiary)' }}>
          {app.nextActionDate ? `Due ${app.nextActionDate}` : '—'}
        </span>
      )
    },
    {
      header: 'Actions',
      width: '16%',
      align: 'right',
      render: (app) => (
        <div style={{ display: 'flex', gap: 'var(--space-1)', justifyContent: 'flex-end' }}>
          <button
            className="btn-secondary btn-sm"
            onClick={() => handleOpenEdit(app)}
            title="Update Stage / Notes"
          >
            Update
          </button>
          <button
            className="btn-danger btn-sm"
            onClick={() => handleDelete(app.id, app.opportunity ? app.opportunity.title : 'this application')}
            title="Delete"
          >
            <span className="material-symbols-outlined" style={{ fontSize: '14px' }}>delete</span>
          </button>
        </div>
      )
    }
  ];

  return (
    <div>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--space-5)' }}>
        <div>
          <h2 style={{ fontSize: '22px', fontWeight: 600 }}>Application Tracker</h2>
          <p style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>
            Manage your opportunities lifecycle: To Apply, Applied, Assessment, Interview, Selected
          </p>
        </div>
        <button className="btn-primary" onClick={() => onNavigate('opportunities')}>
          <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>add</span>
          Add Opportunities
        </button>
      </div>

      {error && (
        <div className="alert alert-danger">
          <span className="material-symbols-outlined">error</span>
          <div>{error}</div>
        </div>
      )}

      {/* Filter bar */}
      <div style={{
        display: 'flex',
        flexWrap: 'wrap',
        gap: 'var(--space-3)',
        alignItems: 'center',
        justifyContent: 'space-between',
        marginBottom: 'var(--space-4)'
      }}>
        <div style={{ display: 'flex', gap: 'var(--space-2)' }}>
          <select
            value={statusFilter}
            onChange={(e) => { setStatusFilter(e.target.value); setPage(0); }}
            style={{ width: '160px' }}
            aria-label="Filter by application status"
          >
            <option value="">All Statuses</option>
            <option value="TO_APPLY">To Apply</option>
            <option value="APPLIED">Applied</option>
            <option value="ASSESSMENT">Assessment</option>
            <option value="INTERVIEW">Interview</option>
            <option value="SELECTED">Selected</option>
            <option value="REJECTED">Rejected</option>
            <option value="WITHDRAWN">Withdrawn</option>
          </select>

          <select
            value={typeFilter}
            onChange={(e) => { setTypeFilter(e.target.value); setPage(0); }}
            style={{ width: '140px' }}
            aria-label="Filter by opportunity type"
          >
            <option value="">All Types</option>
            <option value="JOB">Jobs</option>
            <option value="INTERNSHIP">Internships</option>
            <option value="HACKATHON">Hackathons</option>
            <option value="CTF">CTFs</option>
          </select>
        </div>

        <select
          value={sort}
          onChange={(e) => { setSort(e.target.value); setPage(0); }}
          style={{ width: '160px' }}
          aria-label="Sort by"
        >
          <option value="createdAt,desc">Recently Tracked</option>
          <option value="nextActionDate,asc">Next Action (Soonest)</option>
          <option value="appliedAt,desc">Applied Date</option>
        </select>
      </div>

      {/* Applications Table */}
      <DataTable
        columns={columns}
        data={applications}
        loading={loading}
        emptyMessage="No applications currently matching filter."
        emptyIcon="assignment"
      />

      <Pagination
        page={page}
        totalPages={totalPages}
        totalElements={totalElements}
        onPageChange={setPage}
      />

      {/* Edit Status / Notes Modal */}
      {editingApp && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--space-4)' }}>
              <h3 style={{ fontSize: '18px', fontWeight: 600 }}>
                Update Application Status
              </h3>
              <button
                className="btn-secondary btn-sm"
                onClick={() => setEditingApp(null)}
                style={{ padding: '4px' }}
              >
                <span className="material-symbols-outlined">close</span>
              </button>
            </div>

            <div style={{
              padding: 'var(--space-3)',
              backgroundColor: 'var(--bg-surface-subtle)',
              borderRadius: 'var(--radius-sm)',
              marginBottom: 'var(--space-4)',
              fontSize: '13px'
            }}>
              <strong>{editingApp.opportunity ? editingApp.opportunity.title : 'Opportunity'}</strong>
              <div style={{ color: 'var(--text-secondary)' }}>
                {editingApp.opportunity ? editingApp.opportunity.organization : ''}
              </div>
            </div>

            <form onSubmit={handleSaveEdit}>
              <div className="form-group">
                <label>Current Stage / Status *</label>
                <select
                  value={editStatus}
                  onChange={(e) => setEditStatus(e.target.value)}
                  required
                >
                  <option value="TO_APPLY">To Apply (Saved)</option>
                  <option value="APPLIED">Applied (Application submitted)</option>
                  <option value="ASSESSMENT">Assessment (OA / Take-home test)</option>
                  <option value="INTERVIEW">Interview (Technical / Behavioral)</option>
                  <option value="SELECTED">Selected (Offer received)</option>
                  <option value="REJECTED">Rejected</option>
                  <option value="WITHDRAWN">Withdrawn</option>
                </select>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 'var(--space-3)' }}>
                <div className="form-group">
                  <label>Date Applied</label>
                  <input
                    type="date"
                    value={editAppliedAt}
                    onChange={(e) => setEditAppliedAt(e.target.value)}
                  />
                </div>

                <div className="form-group">
                  <label>Next Action Date</label>
                  <input
                    type="date"
                    value={editNextAction}
                    onChange={(e) => setEditNextAction(e.target.value)}
                  />
                </div>
              </div>

              <div className="form-group">
                <label>Operational Notes & Log</label>
                <textarea
                  rows={4}
                  value={editNotes}
                  onChange={(e) => setEditNotes(e.target.value)}
                  placeholder="e.g. Completed round 1 phone screen; preparing system design notes..."
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 'var(--space-3)', marginTop: 'var(--space-5)' }}>
                <button type="button" className="btn-secondary" onClick={() => setEditingApp(null)}>
                  Cancel
                </button>
                <button type="submit" className="btn-primary" disabled={saving}>
                  {saving ? 'Updating...' : 'Save Updates'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
