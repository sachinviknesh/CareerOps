import React from 'react';

export function StatusBadge({ status, type }) {
  if (!status && !type) return null;

  const value = status || type;
  let variant = 'badge-neutral';

  switch (value) {
    case 'SELECTED':
    case 'JOB':
      variant = 'badge-success';
      break;
    case 'INTERVIEW':
    case 'ASSESSMENT':
    case 'INTERNSHIP':
      variant = 'badge-info';
      break;
    case 'TO_APPLY':
    case 'HACKATHON':
    case 'CTF':
      variant = 'badge-warning';
      break;
    case 'REJECTED':
      variant = 'badge-danger';
      break;
    case 'APPLIED':
    case 'WITHDRAWN':
    case 'OTHER':
    default:
      variant = 'badge-neutral';
      break;
  }

  const formatText = (text) => {
    return text.replace(/_/g, ' ');
  };

  return (
    <span className={`badge ${variant}`}>
      {formatText(value)}
    </span>
  );
}
