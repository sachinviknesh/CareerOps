import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import { DataTable } from '../components/DataTable';
import { Pagination } from '../components/Pagination';
import { SearchBar } from '../components/SearchBar';
import { StatusBadge } from '../components/StatusBadge';

export function Opportunities({ onNavigate, onSelectOpportunity }) {
  const [opportunities, setOpportunities] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Filters
  const [search, setSearch] = useState('');
  const [type, setType] = useState('');
  const [roleFamily, setRoleFamily] = useState('');
  const [sort, setSort] = useState('createdAt,desc');

  // Modal for Create/Edit
  const [modalOpen, setModalOpen] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [allSkills, setAllSkills] = useState([]);
  const [formData, setFormData] = useState({
    title: '',
    organization: '',
    type: 'JOB',
    roleFamily: '',
    description: '',
    location: '',
    workMode: 'Hybrid',
    url: '',
    deadline: '',
    active: true,
    requiredSkillIds: []
  });
  const [formSubmitting, setFormSubmitting] = useState(false);
  const [formError, setFormError] = useState(null);

  useEffect(() => {
    loadOpportunities();
  }, [page, type, roleFamily, sort]);

  useEffect(() => {
    loadSkills();
  }, []);

  const loadSkills = async () => {
    try {
      const skills = await api.getSkills();
      setAllSkills(skills || []);
    } catch (e) {
      console.error('Failed to load skills', e);
    }
  };

  const loadOpportunities = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await api.getOpportunities({
        page,
        size: 10,
        sort,
        search: search.trim() || undefined,
        type: type || undefined,
        roleFamily: roleFamily.trim() || undefined
      });
      setOpportunities(res.content || []);
      setTotalPages(res.totalPages || 0);
      setTotalElements(res.totalElements || 0);
    } catch (err) {
      setError(err.message || 'Failed to load opportunities');
    } finally {
      setLoading(false);
    }
  };

  const handleSearchSubmit = () => {
    setPage(0);
    loadOpportunities();
  };

  const handleOpenCreate = () => {
    setEditingId(null);
    setFormData({
      title: '',
      organization: '',
      type: 'JOB',
      roleFamily: '',
      description: '',
      location: '',
      workMode: 'Hybrid',
      url: '',
      deadline: '',
      active: true,
      requiredSkillIds: []
    });
    setFormError(null);
    setModalOpen(true);
  };

  const handleOpenEdit = (opp) => {
    setEditingId(opp.id);
    setFormData({
      title: opp.title,
      organization: opp.organization || '',
      type: opp.type,
      roleFamily: opp.roleFamily || '',
      description: opp.description || '',
      location: opp.location || '',
      workMode: opp.workMode || '',
      url: opp.url || '',
      deadline: opp.deadline || '',
      active: opp.active !== false,
      requiredSkillIds: opp.requiredSkills ? Array.from(opp.requiredSkills).map(s => s.id) : []
    });
    setFormError(null);
    setModalOpen(true);
  };

  const handleDelete = async (id, title) => {
    if (!window.confirm(`Are you sure you want to delete "${title}"?`)) return;
    try {
      await api.deleteOpportunity(id);
      loadOpportunities();
    } catch (err) {
      alert(err.message || 'Failed to delete opportunity');
    }
  };

  const handleFormSubmit = async (e) => {
    e.preventDefault();
    setFormSubmitting(true);
    setFormError(null);

    try {
      const payload = {
        ...formData,
        deadline: formData.deadline || null,
        requiredSkillIds: formData.requiredSkillIds.map(Number)
      };

      if (editingId) {
        await api.updateOpportunity(editingId, payload);
      } else {
        await api.createOpportunity(payload);
      }

      setModalOpen(false);
      loadOpportunities();
    } catch (err) {
      setFormError(err.message || 'Failed to save opportunity');
    } finally {
      setFormSubmitting(false);
    }
  };

  const toggleSkillSelect = (skillId) => {
    setFormData(prev => {
      const exists = prev.requiredSkillIds.includes(skillId);
      return {
        ...prev,
        requiredSkillIds: exists
          ? prev.requiredSkillIds.filter(id => id !== skillId)
          : [...prev.requiredSkillIds, skillId]
      };
    });
  };

  const columns = [
    {
      header: 'Opportunity',
      width: '28%',
      render: (opp) => (
        <div>
          <div style={{ fontWeight: 600, color: 'var(--text-primary)' }}>{opp.title}</div>
          <div style={{ fontSize: '12px', color: 'var(--text-tertiary)' }}>
            {opp.location || 'Remote'} {opp.workMode ? `• ${opp.workMode}` : ''}
          </div>
        </div>
      )
    },
    {
      header: 'Organization',
      width: '18%',
      render: (opp) => <span style={{ color: 'var(--text-secondary)' }}>{opp.organization || 'Independent'}</span>
    },
    {
      header: 'Type',
      width: '12%',
      render: (opp) => <StatusBadge type={opp.type} />
    },
    {
      header: 'Role Family',
      width: '14%',
      render: (opp) => <span style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>{opp.roleFamily || 'General'}</span>
    },
    {
      header: 'Deadline',
      width: '12%',
      render: (opp) => (
        <span className="mono" style={{ fontSize: '12px', color: opp.deadline ? 'var(--text-primary)' : 'var(--text-tertiary)' }}>
          {opp.deadline || 'Rolling'}
        </span>
      )
    },
    {
      header: 'Actions',
      width: '16%',
      align: 'right',
      render: (opp) => (
        <div style={{ display: 'flex', gap: 'var(--space-1)', justifyContent: 'flex-end' }}>
          <button
            className="btn-secondary btn-sm"
            onClick={(e) => {
              e.stopPropagation();
              onSelectOpportunity(opp.id);
              onNavigate('opportunityDetail');
            }}
            title="View Details & Analyze Fit"
          >
            Fit & View
          </button>
          <button
            className="btn-secondary btn-sm"
            onClick={(e) => {
              e.stopPropagation();
              handleOpenEdit(opp);
            }}
            title="Edit"
          >
            <span className="material-symbols-outlined" style={{ fontSize: '14px' }}>edit</span>
          </button>
          <button
            className="btn-danger btn-sm"
            onClick={(e) => {
              e.stopPropagation();
              handleDelete(opp.id, opp.title);
            }}
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
          <h2 style={{ fontSize: '22px', fontWeight: 600 }}>Opportunities Catalog</h2>
          <p style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>
            Search, filter, and compare candidate suitability across industry roles
          </p>
        </div>
        <button className="btn-primary" onClick={handleOpenCreate}>
          <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>add</span>
          New Opportunity
        </button>
      </div>

      {error && (
        <div className="alert alert-danger">
          <span className="material-symbols-outlined">error</span>
          <div>{error}</div>
        </div>
      )}

      {/* Search & Filter Toolbar */}
      <SearchBar
        value={search}
        onChange={setSearch}
        onSearch={handleSearchSubmit}
        placeholder="Filter by title, stack, or keywords..."
        filters={
          <>
            <select
              value={type}
              onChange={(e) => { setType(e.target.value); setPage(0); }}
              style={{ width: '130px' }}
              aria-label="Filter by type"
            >
              <option value="">All Types</option>
              <option value="JOB">Job</option>
              <option value="INTERNSHIP">Internship</option>
              <option value="HACKATHON">Hackathon</option>
              <option value="CTF">CTF</option>
              <option value="OTHER">Other</option>
            </select>

            <select
              value={sort}
              onChange={(e) => { setSort(e.target.value); setPage(0); }}
              style={{ width: '150px' }}
              aria-label="Sort by"
            >
              <option value="createdAt,desc">Newest First</option>
              <option value="deadline,asc">Deadline (Soonest)</option>
              <option value="title,asc">Title (A-Z)</option>
              <option value="organization,asc">Organization (A-Z)</option>
            </select>
          </>
        }
      />

      {/* Main Table */}
      <DataTable
        columns={columns}
        data={opportunities}
        loading={loading}
        emptyMessage="No opportunities matching current criteria."
        onRowClick={(row) => {
          onSelectOpportunity(row.id);
          onNavigate('opportunityDetail');
        }}
      />

      {/* Pagination */}
      <Pagination
        page={page}
        totalPages={totalPages}
        totalElements={totalElements}
        onPageChange={setPage}
      />

      {/* Create / Edit Modal */}
      {modalOpen && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--space-4)' }}>
              <h3 style={{ fontSize: '18px', fontWeight: 600 }}>
                {editingId ? 'Edit Opportunity' : 'Create New Opportunity'}
              </h3>
              <button
                className="btn-secondary btn-sm"
                onClick={() => setModalOpen(false)}
                style={{ padding: '4px' }}
              >
                <span className="material-symbols-outlined">close</span>
              </button>
            </div>

            {formError && (
              <div className="alert alert-danger">
                <span className="material-symbols-outlined">error</span>
                <div>{formError}</div>
              </div>
            )}

            <form onSubmit={handleFormSubmit}>
              <div className="form-group">
                <label>Opportunity Title *</label>
                <input
                  type="text"
                  required
                  value={formData.title}
                  onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                  placeholder="e.g. Associate Backend Engineer"
                />
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 'var(--space-3)' }}>
                <div className="form-group">
                  <label>Organization / Company</label>
                  <input
                    type="text"
                    value={formData.organization}
                    onChange={(e) => setFormData({ ...formData, organization: e.target.value })}
                    placeholder="e.g. Acme Corp"
                  />
                </div>

                <div className="form-group">
                  <label>Type *</label>
                  <select
                    value={formData.type}
                    onChange={(e) => setFormData({ ...formData, type: e.target.value })}
                  >
                    <option value="JOB">Job</option>
                    <option value="INTERNSHIP">Internship</option>
                    <option value="HACKATHON">Hackathon</option>
                    <option value="CTF">CTF</option>
                    <option value="OTHER">Other</option>
                  </select>
                </div>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 'var(--space-3)' }}>
                <div className="form-group">
                  <label>Role Family / Domain</label>
                  <input
                    type="text"
                    value={formData.roleFamily}
                    onChange={(e) => setFormData({ ...formData, roleFamily: e.target.value })}
                    placeholder="e.g. Backend Development, Cloud, AI"
                  />
                </div>

                <div className="form-group">
                  <label>Location</label>
                  <input
                    type="text"
                    value={formData.location}
                    onChange={(e) => setFormData({ ...formData, location: e.target.value })}
                    placeholder="e.g. Bengaluru, IN / Remote"
                  />
                </div>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 'var(--space-3)' }}>
                <div className="form-group">
                  <label>Work Mode</label>
                  <input
                    type="text"
                    value={formData.workMode}
                    onChange={(e) => setFormData({ ...formData, workMode: e.target.value })}
                    placeholder="Remote / Hybrid / On-site"
                  />
                </div>

                <div className="form-group">
                  <label>Application Deadline</label>
                  <input
                    type="date"
                    value={formData.deadline}
                    onChange={(e) => setFormData({ ...formData, deadline: e.target.value })}
                  />
                </div>
              </div>

              <div className="form-group">
                <label>External URL / Job Listing</label>
                <input
                  type="url"
                  value={formData.url}
                  onChange={(e) => setFormData({ ...formData, url: e.target.value })}
                  placeholder="https://..."
                />
              </div>

              <div className="form-group">
                <label>Job Description & Responsibilities</label>
                <textarea
                  rows={4}
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  placeholder="Paste job description, prerequisites, and expectations..."
                />
              </div>

              {/* Skills association selector */}
              <div className="form-group">
                <label>Required Skills (Click to select)</label>
                <div style={{
                  maxHeight: '140px',
                  overflowY: 'auto',
                  border: '1px solid var(--border-subtle)',
                  borderRadius: 'var(--radius-sm)',
                  padding: 'var(--space-2)',
                  display: 'flex',
                  flexWrap: 'wrap',
                  gap: '6px',
                  backgroundColor: 'var(--bg-surface-subtle)'
                }}>
                  {allSkills.map(skill => {
                    const isSelected = formData.requiredSkillIds.includes(skill.id);
                    return (
                      <button
                        type="button"
                        key={skill.id}
                        className={`badge ${isSelected ? 'badge-info' : 'badge-neutral'}`}
                        style={{ cursor: 'pointer', padding: '4px 10px', fontSize: '12px' }}
                        onClick={() => toggleSkillSelect(skill.id)}
                      >
                        {skill.name} {isSelected && '✓'}
                      </button>
                    );
                  })}
                  {allSkills.length === 0 && (
                    <span style={{ fontSize: '12px', color: 'var(--text-tertiary)' }}>
                      No skills available. Create skills in Profile/Skills.
                    </span>
                  )}
                </div>
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 'var(--space-3)', marginTop: 'var(--space-5)' }}>
                <button type="button" className="btn-secondary" onClick={() => setModalOpen(false)}>
                  Cancel
                </button>
                <button type="submit" className="btn-primary" disabled={formSubmitting}>
                  {formSubmitting ? 'Saving...' : editingId ? 'Update Opportunity' : 'Create Opportunity'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
