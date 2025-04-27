import React from 'react';
import { Box } from '@mui/material';

const BINGO_RANGES = [
  [1, 15],
  [16, 30],
  [31, 45],
  [46, 60],
  [61, 75],
];

const BINGO_COLORS = ['#FF5252', '#FF9800', '#FF5BCB', '#4CAF50', '#E9D100'];

type BingoBallSize = 'small' | 'medium' | 'large';

interface BingoBallProps {
  value: number;
  size?: BingoBallSize;
  onClick?: () => void;
}

const BALL_SIZES: Record<BingoBallSize, number> = {
  small: 40,
  medium: 60,
  large: 80,
};

const BingoBall: React.FC<BingoBallProps> = ({ value, size = 'medium', onClick }) => {
  const rangeIndex = BINGO_RANGES.findIndex(([min, max]) => value >= min && value <= max);
  const color = BINGO_COLORS[rangeIndex];
  const ballSize = BALL_SIZES[size];
  const innerCircleSize = ballSize * 0.65;

  return (
    <Box
      onClick={onClick}
      sx={{
        width: ballSize,
        height: ballSize,
        borderRadius: '50%',
        backgroundColor: color,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        position: 'relative',
        boxShadow: '2px 2px 5px rgba(0,0,0,0.2)',
        cursor: onClick ? 'pointer' : 'default',
        transition: 'transform 0.2s ease',
        '&:hover': {
          transform: 'scale(1.05)',
        },
        '&::before': {
          content: '""',
          position: 'absolute',
          top: '10%',
          left: '10%',
          width: '40%',
          height: '40%',
          borderRadius: '50%',
          background: 'radial-gradient(circle at 30% 30%, rgba(255,255,255,0.4) 0%, rgba(255,255,255,0) 70%)',
          pointerEvents: 'none',
        },
      }}
    >
      <Box
        sx={{
          width: innerCircleSize,
          height: innerCircleSize,
          borderRadius: '50%',
          backgroundColor: 'white',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          boxShadow: 'inset 1px 1px 2px rgba(0,0,0,0.2)',
          fontSize: ballSize * 0.35,
          fontWeight: 'bold',
          color: 'black',
        }}
      >
        {value}
      </Box>
    </Box>
  );
};

export default BingoBall; 