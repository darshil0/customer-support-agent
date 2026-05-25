import React from 'react';

interface SparklineProps {
  data: number[];
  color?: string;
  height?: number;
  width?: number;
  showFill?: boolean;
}

export const Sparkline: React.FC<SparklineProps> = ({
  data,
  color = '#3b82f6',
  height = 40,
  width = 100,
  showFill = true,
}) => {
  const gradientId = React.useId().replace(/:/g, '');

  if (!data || data.length === 0) {
    return (
      <svg width={width} height={height} viewBox={`0 0 ${width} ${height}`}>
        <text x={width / 2} y={height / 2} textAnchor="middle" fill="#999">
          No data
        </text>
      </svg>
    );
  }

  // Normalize data to fit within height
  const min = Math.min(...data);
  const max = Math.max(...data);
  const range = max - min || 1;

  // Create points for polyline
  const points = data
    .map((value, index) => {
      const x = (index / (data.length - 1)) * width;
      const y = height - ((value - min) / range) * height;
      return `${x},${y}`;
    })
    .join(' ');

  // Sanitized color
  const cleanColor = color.startsWith('#') ? color : `#${color}`;

  return (
    <svg
      width={width}
      height={height}
      viewBox={`0 0 ${width} ${height}`}
      className="sparkline"
      preserveAspectRatio="none"
    >
      <defs>
        <linearGradient id={gradientId} x1="0%" y1="0%" x2="0%" y2="100%">
          <stop offset="0%" stopColor={cleanColor} stopOpacity="0.5" />
          <stop offset="100%" stopColor={cleanColor} stopOpacity="0.05" />
        </linearGradient>
      </defs>

      {showFill && (
        <polygon
          points={`0,${height} ${points} ${width},${height}`}
          fill={`url(#${gradientId})`}
        />
      )}

      <polyline
        points={points}
        fill="none"
        stroke={cleanColor}
        strokeWidth="2"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  );
};
