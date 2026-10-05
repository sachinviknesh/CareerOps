import React, { useState, useEffect } from 'react';
import { api } from '../api/client';

export function Profile({ onNavigate }) {
  const [profile, setProfile] = useState(null);
  const [skills, setSkills] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState(null);
  const [error, setError] = useState(null);

  // Profile form state
  const [formData, setFormData] = useState({
    name: '',
    headline: '',
    location: '',
    education: '',
    experienceYears: 0,
    targetRoles: '',
    skillIds: []
  });

  // New Skill creator state
  const [newSkillName, setNewSkillName] = useState('');
  const [newSkillCategory, setNewSkillCategory] = useState('Programming');
  const [creatingSkill, setCreatingSkill] = useState(false);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [profData, skillsData] = await Promise.all([
        api.getProfile(),
        api.getSkills()
      ]);
      setProfile(profData);
      setSkills(skillsData || []);

      if (profData) {
        setFormData({
          name: profData.name || '',
          headline: profData.headline || '',
          location: profData.location || '',
          education: profData.education || '',
          experienceYears: profData.experienceYears || 0,
          targetRoles: profData.targetRoles || '',
          skillIds: profData.skills ? Array.from(profData.skills).map(s => s.id) : []
        });
      }
    } catch (err) {
      setError(err.message || 'Failed to load profile');
    } finally {
      setLoading(false);
    }
  };

  const handleProfileSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setMessage(null);
    setError(null);

    try {
      const updated = await api.updateProfile({
        name: formData.name,
        headline: formData.headline,
        location: formData.location,
        education: formData.education,
        experienceYears: Number(formData.experienceYears),
        targetRoles: formData.targetRoles,
        skillIds: formData.skillIds.map(Number)
      });
      setProfile(updated);
      setMessage('Profile and linked skills successfully updated!');
    } catch (err) {
      setError(err.message || 'Failed to update profile');
    } finally {
      setSaving(false);
    }
  };

  const handleCreateSkill = async (e) => {
    e.preventDefault();
    if (!newSkillName.trim()) return;
    setCreatingSkill(true);
    try {
      const created = await api.createSkill({
        name: newSkillName.trim(),
        category: newSkillCategory
      });
      setSkills(prev => [...prev, created]);
      // Also automatically add to candidate's selected skills
      setFormData(prev => ({
        ...prev,
        skillIds: [...prev.skillIds, created.id]
      }));
      setNewSkillName('');
    } catch (err) {
      alert(err.message || 'Failed to create skill (may already exist)');
    } finally {
      setCreatingSkill(false);
    }
  };

  const toggleSkill = (skillId) => {
    setFormData(prev => {
      const exists = prev.skillIds.includes(skillId);
      return {
        ...prev,
        skillIds: exists
          ? prev.skillIds.filter(id => id !== skillId)
          : [...prev.skillIds, skillId]
      };
    });
  };

  if (loading) {
    return <div style={{ padding: 'var(--space-6)', textAlign: 'center' }}>Loading candidate profile...</div>;
  }

  return (
    <div>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--space-5)' }}>
        <div>
          <h2 style={{ fontSize: '22px', fontWeight: 600 }}>Candidate Profile & Technical Skills</h2>
          <p style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>
            Maintain education, target domain, and skills for accurate job-fit matching
          </p>
        </div>
        <button className="btn-secondary btn-sm" onClick={() => onNavigate('resume')}>
          <span className="material-symbols-outlined" style={{ fontSize: '16px' }}>upload_file</span>
          Manage Resume
        </button>
      </div>

      {message && (
        <div className="alert alert-success">
          <span className="material-symbols-outlined">check_circle</span>
          <div>{message}</div>
        </div>
      )}

      {error && (
        <div className="alert alert-danger">
          <span className="material-symbols-outlined">error</span>
          <div>{error}</div>
        </div>
      )}

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(350px, 1fr))', gap: 'var(--space-6)' }}>
        {/* Form Details */}
        <div style={{
          backgroundColor: 'var(--bg-surface)',
          border: '1px solid var(--border-subtle)',
          borderRadius: 'var(--radius-lg)',
          padding: 'var(--space-6)',
          boxShadow: 'var(--shadow-card)'
        }}>
          <h3 style={{ fontSize: '16px', fontWeight: 600, marginBottom: 'var(--space-4)' }}>
            Profile Information
          </h3>

          <form onSubmit={handleProfileSubmit}>
            <div className="form-group">
              <label>Full Name *</label>
              <input
                type="text"
                required
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              />
            </div>

            <div className="form-group">
              <label>Professional Headline</label>
              <input
                type="text"
                value={formData.headline}
                onChange={(e) => setFormData({ ...formData, headline: e.target.value })}
                placeholder="e.g. Aspiring Backend Software Engineer | Java & Spring Boot"
              />
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 'var(--space-3)' }}>
              <div className="form-group">
                <label>Location</label>
                <input
                  type="text"
                  value={formData.location}
                  onChange={(e) => setFormData({ ...formData, location: e.target.value })}
                  placeholder="e.g. Pune, India"
                />
              </div>

              <div className="form-group">
                <label>Experience (Years)</label>
                <input
                  type="number"
                  min="0"
                  max="50"
                  value={formData.experienceYears}
                  onChange={(e) => setFormData({ ...formData, experienceYears: e.target.value })}
                />
              </div>
            </div>

            <div className="form-group">
              <label>Education & Degree</label>
              <input
                type="text"
                value={formData.education}
                onChange={(e) => setFormData({ ...formData, education: e.target.value })}
                placeholder="e.g. B.Tech in Computer Science, 2026"
              />
            </div>

            <div className="form-group">
              <label>Target Roles (comma-separated)</label>
              <input
                type="text"
                value={formData.targetRoles}
                onChange={(e) => setFormData({ ...formData, targetRoles: e.target.value })}
                placeholder="Backend Developer, Cloud Engineer, SRE"
              />
            </div>

            <div style={{ marginTop: 'var(--space-5)' }}>
              <button type="submit" className="btn-primary" disabled={saving}>
                {saving ? 'Saving...' : 'Save Profile Changes'}
              </button>
            </div>
          </form>
        </div>

        {/* Linked Skills Section */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-5)' }}>
          {/* Selected Skills Matrix */}
          <div style={{
            backgroundColor: 'var(--bg-surface)',
            border: '1px solid var(--border-subtle)',
            borderRadius: 'var(--radius-lg)',
            padding: 'var(--space-6)',
            boxShadow: 'var(--shadow-card)'
          }}>
            <h3 style={{ fontSize: '16px', fontWeight: 600, marginBottom: 'var(--space-2)' }}>
              Linked Candidate Skills ({formData.skillIds.length})
            </h3>
            <p style={{ fontSize: '12px', color: 'var(--text-secondary)', marginBottom: 'var(--space-4)' }}>
              Click to attach or detach skills from your candidate profile:
            </p>

            <div style={{
              display: 'flex',
              flexWrap: 'wrap',
              gap: '6px',
              maxHeight: '220px',
              overflowY: 'auto',
              padding: 'var(--space-3)',
              backgroundColor: 'var(--bg-surface-subtle)',
              borderRadius: 'var(--radius-sm)',
              marginBottom: 'var(--space-4)'
            }}>
              {skills.map((skill) => {
                const isSelected = formData.skillIds.includes(skill.id);
                return (
                  <button
                    type="button"
                    key={skill.id}
                    onClick={() => toggleSkill(skill.id)}
                    className={`badge ${isSelected ? 'badge-success' : 'badge-neutral'}`}
                    style={{ cursor: 'pointer', padding: '4px 10px', fontSize: '12px' }}
                  >
                    {skill.name} {isSelected ? '✓' : '+'}
                  </button>
                );
              })}
              {skills.length === 0 && (
                <div style={{ fontSize: '12px', color: 'var(--text-tertiary)' }}>No skills created yet. Add below!</div>
              )}
            </div>

            <button type="button" className="btn-secondary btn-sm" onClick={handleProfileSubmit} disabled={saving}>
              Sync Selected Skills
            </button>
          </div>

          {/* Quick Create Skill */}
          <div style={{
            backgroundColor: 'var(--bg-surface)',
            border: '1px solid var(--border-subtle)',
            borderRadius: 'var(--radius-lg)',
            padding: 'var(--space-5)',
            boxShadow: 'var(--shadow-subtle)'
          }}>
            <h4 style={{ fontSize: '14px', fontWeight: 600, marginBottom: 'var(--space-3)' }}>
              Add New Skill to Catalog
            </h4>
            <form onSubmit={handleCreateSkill} style={{ display: 'flex', gap: 'var(--space-2)' }}>
              <input
                type="text"
                required
                value={newSkillName}
                onChange={(e) => setNewSkillName(e.target.value)}
                placeholder="Skill name (e.g. Docker, Go)"
                style={{ flex: 2 }}
              />
              <select
                value={newSkillCategory}
                onChange={(e) => setNewSkillCategory(e.target.value)}
                style={{ flex: 1 }}
              >
                <option value="Programming">Programming</option>
                <option value="Framework">Framework</option>
                <option value="Database">Database</option>
                <option value="Cloud">Cloud</option>
                <option value="DevOps">DevOps</option>
                <option value="AI">AI/ML</option>
                <option value="Tool">Tool</option>
                <option value="Other">Other</option>
              </select>
              <button type="submit" className="btn-secondary" disabled={creatingSkill}>
                {creatingSkill ? 'Adding...' : 'Add'}
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
}
