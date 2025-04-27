import React, { useState, useEffect } from 'react';
import {
  Paper,
  Typography,
  Button,
  Box,
  Card,
  CardContent,
  TextField,
  Container,
  Chip,
  IconButton,
  Input,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  List,
  ListItem,
  ListItemText,
  ListItemSecondaryAction,
  Divider,
} from '@mui/material';
import { motion, AnimatePresence } from 'framer-motion';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import EmojiEventsIcon from '@mui/icons-material/EmojiEvents';
import CasinoIcon from '@mui/icons-material/Casino';
import AddCircleIcon from '@mui/icons-material/AddCircle';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import ImageIcon from '@mui/icons-material/Image';
import AddIcon from '@mui/icons-material/Add';
import DeleteIcon from '@mui/icons-material/Delete';
import EditIcon from '@mui/icons-material/Edit';
import AutoFixHighIcon from '@mui/icons-material/AutoFixHigh';
import { useAuth } from '../contexts/AuthContext';
import BingoBall from './BingoBall';
import ReactConfetti from 'react-confetti';

interface BingoCard {
  id: string;
  numbers: (number | '*')[][];
  is_completed: boolean;
  winning_numbers: number[] | null;
  is_played: boolean;
  background_image: string | null;
  magic_mantras: string[];
}

interface PlayingCard extends BingoCard {
  isNew?: boolean;
}

type BingoNumber = number | '*' | '';

interface ValidationErrors {
  [key: string]: boolean;
}

const BINGO_RANGES = [
  [1, 15],   
  [16, 30],  
  [31, 45],  
  [46, 60],  
  [61, 75],  
];

const BINGO_COLORS = ['#FF5252', '#FF9800', '#FF5BCB', '#4CAF50', '#E9D100'];

interface FileInputProps {
  id: string;
  onChange: (event: React.ChangeEvent<HTMLInputElement>) => void;
  style?: React.CSSProperties;
}

const FileInput = React.forwardRef<HTMLInputElement, FileInputProps>(
  ({ id, onChange, style }, ref) => (
    <input
      ref={ref}
      type="file"
      id={id}
      style={{ display: 'none', ...style }}
      onChange={onChange}
      accept="image/*"
    />
  )
);

const Game: React.FC = () => {
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();
  const [cards, setCards] = useState<PlayingCard[]>([]);
  const [currentNumbers, setCurrentNumbers] = useState<BingoNumber[][]>(() => {
    const grid = Array(5).fill(null).map(() => Array(5).fill(''));
    grid[2][2] = '*';
    return grid;
  });
  const [validationErrors, setValidationErrors] = useState<ValidationErrors>({});
  const [winningCard, setWinningCard] = useState<string | null>(null);
  const [showConfetti, setShowConfetti] = useState(false);
  const [drawnNumbers, setDrawnNumbers] = useState<number[]>([]);
  const [revealedNumbers, setRevealedNumbers] = useState<number[]>([]);
  const [isRevealing, setIsRevealing] = useState(false);
  const [activeCard, setActiveCard] = useState<string | null>(null);
  const [highlightedNumbers, setHighlightedNumbers] = useState<number[]>([]);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [previewImage, setPreviewImage] = useState<string | null>(null);
  const [magicMantras, setMagicMantras] = useState<string[]>([]);
  const [newMantra, setNewMantra] = useState('');
  const [mantraDialogOpen, setMantraDialogOpen] = useState(false);
  const [selectedCardId, setSelectedCardId] = useState<string | null>(null);
  const [editingMantraIndex, setEditingMantraIndex] = useState<number | null>(null);

  useEffect(() => {
    if (!isAuthenticated) {
      return;
    }
    fetchCards();
  }, [navigate, isAuthenticated]);

  const fetchCards = async () => {
    try {
      const response = await axios.get('/api/game/cards');
      const updatedCards = response.data.map((card: BingoCard) => ({
        ...card,
        isNew: !cards.some(c => c.id === card.id)
      }));
      setCards(updatedCards);
      if (updatedCards.length > cards.length && cards.length > 0) {
        setTimeout(() => {
          setCards(prev => prev.map(card => ({ ...card, isNew: false })));
        }, 3000);
      }
    } catch (err) {
      console.error('Failed to fetch cards:', err);
    }
  };

  const validateCell = (value: BingoNumber, col: number): boolean => {
    if (value === '' || value === '*') return true;
    if (typeof value !== 'number') return false;
    
    const [min, max] = BINGO_RANGES[col];
    return value >= min && value <= max;
  };

  const validateBingoRules = (numbers: BingoNumber[][]): boolean => {
    const newValidationErrors: ValidationErrors = {};
    let isValid = true;

    const allNumbers: number[] = [];
    for (let col = 0; col < 5; col++) {
      for (let row = 0; row < 5; row++) {
        if (row === 2 && col === 2) continue;
        const num = numbers[row][col];
        if (num === '') {
          newValidationErrors[`${row}-${col}`] = true;
          isValid = false;
          continue;
        }
        if (!validateCell(num, col)) {
          newValidationErrors[`${row}-${col}`] = true;
          isValid = false;
        }
        if (typeof num === 'number') {
          allNumbers.push(num);
        }
      }
    }

    const uniqueNumbers = new Set(allNumbers);
    if (uniqueNumbers.size !== allNumbers.length) {
      allNumbers.forEach((num, index) => {
        if (allNumbers.indexOf(num) !== allNumbers.lastIndexOf(num)) {
          numbers.forEach((row, rowIndex) => {
            row.forEach((cell, colIndex) => {
              if (cell === num) {
                newValidationErrors[`${rowIndex}-${colIndex}`] = true;
              }
            });
          });
        }
      });
      isValid = false;
    }

    setValidationErrors(newValidationErrors);
    return isValid;
  };

  const handleNumberChange = (row: number, col: number, value: string) => {
    if (row === 2 && col === 2) return;
    
    const newNumbers = currentNumbers.map((r) => [...r]);
    const numValue = value === '' ? '' : parseInt(value);
    newNumbers[row][col] = numValue;
    setCurrentNumbers(newNumbers);
    
    validateBingoRules(newNumbers);
  };

  const handleFileSelect = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (file) {
      setSelectedFile(file);
      const reader = new FileReader();
      reader.onloadend = () => {
        setPreviewImage(reader.result as string);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleCardBackgroundChange = (cardId: string) => (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onloadend = () => {
        handleUpdateBackground(cardId, reader.result as string);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleCreateCard = async () => {
    if (!validateBingoRules(currentNumbers)) {
      return;
    }

    try {
      await axios.post('/api/game/card', {
        numbers: currentNumbers,
        background_image: previewImage,
        magic_mantras: magicMantras
      });
      const newGrid = Array(5).fill(null).map(() => Array(5).fill(''));
      newGrid[2][2] = '*';
      setCurrentNumbers(newGrid);
      setValidationErrors({});
      setPreviewImage(null);
      setSelectedFile(null);
      setMagicMantras([]);
      
      const cardsResponse = await axios.get('/api/game/cards');
      const newCards = cardsResponse.data.map((card: BingoCard) => ({
        ...card,
        isNew: !cards.some(c => c.id === card.id)
      }));
      setCards(newCards);
      
      setTimeout(() => {
        setCards(prev => prev.map(card => ({ ...card, isNew: false })));
      }, 1000);
    } catch (err) {
      console.error('Failed to create card:', err);
    }
  };

  const handleUpdateBackground = async (cardId: string, imageData: string) => {
    try {
      await axios.put(`/api/game/card/${cardId}/background`, {
        background_image: imageData
      });
      await fetchCards();
    } catch (err) {
      console.error('Failed to update background:', err);
    }
  };

  const handleOpenMantraDialog = (cardId: string) => {
    const card = cards.find(c => c.id === cardId);
    if (card) {
      setMagicMantras(card.magic_mantras || []);
      setSelectedCardId(cardId);
      setMantraDialogOpen(true);
    }
  };

  const handleCloseMantraDialog = () => {
    setMantraDialogOpen(false);
    setSelectedCardId(null);
    setEditingMantraIndex(null);
    setNewMantra('');
  };

  const handleAddMantra = () => {
    if (newMantra.trim()) {
      if (editingMantraIndex !== null) {
        const updatedMantras = [...magicMantras];
        updatedMantras[editingMantraIndex] = newMantra.trim();
        setMagicMantras(updatedMantras);
        setEditingMantraIndex(null);
      } else {
        setMagicMantras([...magicMantras, newMantra.trim()]);
      }
      setNewMantra('');
    }
  };

  const handleEditMantra = (index: number) => {
    setNewMantra(magicMantras[index]);
    setEditingMantraIndex(index);
  };

  const handleDeleteMantra = (index: number) => {
    const updatedMantras = magicMantras.filter((_, i) => i !== index);
    setMagicMantras(updatedMantras);
  };

  const handleSaveMantras = async () => {
    if (selectedCardId) {
      try {
        console.log('Saving mantras for card:', selectedCardId);
        console.log('Mantras to save:', magicMantras);
        
        const response = await axios.put(`/api/game/card/${selectedCardId}/mantras`, {
          magic_mantras: magicMantras
        });
        
        console.log('Save response:', response.data);
        await fetchCards();
        handleCloseMantraDialog();
      } catch (err) {
        console.error('Failed to update mantras:', err);
      }
    } else {
      handleCloseMantraDialog();
    }
  };

  const revealNextNumber = (numbers: number[], currentIndex: number, winningCardId: string | null) => {
    if (currentIndex >= numbers.length) {
      setIsRevealing(false);
      if (winningCardId !== null) {
        setWinningCard(winningCardId);
        setShowConfetti(true);
        setTimeout(() => {
          setShowConfetti(false);
        }, 60000);
      }
      return;
    }

    const newNumber = numbers[currentIndex];
    setRevealedNumbers(prev => [...prev, newNumber]);
    setTimeout(() => {
      setHighlightedNumbers(prev => [...prev, newNumber]);
    }, 250);
    setTimeout(() => revealNextNumber(numbers, currentIndex + 1, winningCardId), 500);
  };

  const handlePlayCard = async (cardId: string) => {
    try {
      setActiveCard(cardId);
      setIsRevealing(true);
      setRevealedNumbers([]);
      setHighlightedNumbers([]);
      
      const response = await axios.post(`/api/game/play/${cardId}`);
      const drawnNumbers = response.data.drawn_numbers;
      setDrawnNumbers(drawnNumbers);
      
      revealNextNumber(drawnNumbers, 0, response.data.status === 'win' ? cardId : null);
      
      const updatedCard = response.data.card;
      setCards(prev => prev.map(card => 
        card.id === cardId ? { ...card, winning_numbers: drawnNumbers, is_played: true } : card
      ));
    } catch (err) {
      console.error('Failed to play card:', err);
      setIsRevealing(false);
    }
  };

  const handleApplyCard = async (cardId: string) => {
    try {
      await axios.post(`/api/game/apply/${cardId}`);
      setRevealedNumbers([]);
      setHighlightedNumbers([]);
      setActiveCard(null);
      setShowConfetti(false);
      await fetchCards();
    } catch (err) {
      console.error('Failed to apply card:', err);
    }
  };

  const generateRandomCard = () => {
    const newGrid = Array(5).fill(null).map(() => Array(5).fill(''));
    newGrid[2][2] = '*';

    for (let col = 0; col < 5; col++) {
      const [min, max] = BINGO_RANGES[col];
      const numbers = Array.from({ length: max - min + 1 }, (_, i) => min + i);
      
      for (let row = 0; row < 5; row++) {
        if (row === 2 && col === 2) continue;
        const randomIndex = Math.floor(Math.random() * numbers.length);
        newGrid[row][col] = numbers.splice(randomIndex, 1)[0];
      }
    }
    
    setCurrentNumbers(newGrid);
    setValidationErrors({});
  };

  return (
    <Container maxWidth="lg" sx={{ 
      pb: 5,
      flex: 1,
      display: 'flex',
      flexDirection: 'column'
    }}>
      {showConfetti && (
        <ReactConfetti
          width={window.innerWidth}
          height={window.innerHeight}
          numberOfPieces={1000}
          recycle={true}
          colors={BINGO_COLORS}
          style={{ position: 'fixed', top: 0, left: 0, zIndex: 9999 }}
        />
      )}
      <Box 
        component={motion.div}
        initial={{ y: -20, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        transition={{ duration: 0.5 }}
        sx={{ 
          display: 'flex', 
          justifyContent: 'space-between', 
          alignItems: 'center', 
          mb: 4,
          mt: 3,
          borderBottom: '3px solid #4CAF50',
          pb: 2
        }}
      >
        <Typography 
          variant="h3" 
          component={motion.h3}
          whileHover={{ scale: 1.05 }}
          sx={{ 
            fontWeight: 'bold',
            color: '#4CAF50',
            fontFamily: '"Press Start 2P", "Roboto", sans-serif',
            textShadow: '2px 2px 4px rgba(0,0,0,0.2)'
          }}
        >
          КапиБинго
        </Typography>
        <Button
          variant="contained"
          color="secondary"
          onClick={() => navigate('/rules')}
          component={motion.button}
          whileHover={{ scale: 1.05 }}
          whileTap={{ scale: 0.95 }}
          startIcon={<EmojiEventsIcon />}
          sx={{ 
            borderRadius: '20px',
            px: 3
          }}
        >
          Правила игры
        </Button>
      </Box>

      <Box sx={{minHeight: '200px'}}>
            <Paper 
              sx={{ 
                p: 3, 
                mb: 3, 
                minHeight: '108px',
                borderRadius: '30px',
                background: '#1a1a1a',
                border: '12px solid #333',
                boxShadow: '0 0 0 2px #111, inset 0 0 20px rgba(0, 0, 0, 0.5)',
                position: 'relative',
                '&::before': {
                  content: '""',
                  position: 'absolute',
                  top: '50%',
                  left: '-15px',
                  width: '3px',
                  height: '40px',
                  background: '#444',
                  transform: 'translateY(-50%)',
                  borderRadius: '3px'
                }
              }}
            >
              <Typography 
                variant="h6" 
                gutterBottom 
                sx={{ 
                  color: '#4CAF50',
                  textAlign: 'center',
                  textTransform: 'uppercase',
                  letterSpacing: '2px',
                  fontWeight: 'bold',
                  textShadow: '0 0 10px rgba(76, 175, 80, 0.5)'
                }}
              >
                CapyTablet
              </Typography>
              <Box 
                sx={{ 
                  display: 'flex',
                  flexWrap: 'wrap',
                  minHeight: '180px',
                  gap: 1,
                  justifyContent: 'center',
                  borderRadius: '20px',
                  p: 2,
                  position: 'relative',
                  overflow: 'hidden',
                  background: '#000'
                }}
              >
                <video
                autoPlay
                muted
                onEnded={(e) => {
                    const video = e.currentTarget;
                    video.currentTime = 0.5;
                    video.play().catch(e => console.error("Video play failed:", e));
                }}
                style={{
                    backfaceVisibility: 'hidden',
                    position: 'absolute',
                    width: '100%',
                    height: '100%',
                    objectFit: 'cover',
                    top: 0,
                    left: 0,
                    zIndex: 0,
                    transform: 'translate3d(0, 0, 0)',
                    willChange: 'transform',
                }}
                >
                <source src="/videos/bingoback.mp4" type="video/mp4" />
                </video>
                <Box sx={{ position: 'relative', zIndex: 1, display: 'flex', flexWrap: 'wrap', gap: 1, justifyContent: 'center' }}>
                  {revealedNumbers.map((number, index) => (
                    <motion.div
                      key={number}
                      initial={{ scale: 0, rotate: -180 }}
                      animate={{ scale: 1, rotate: 0 }}
                      transition={{ duration: 0.5, type: 'spring' }}
                    >
                      <BingoBall value={number} size="medium" />
                    </motion.div>
                  ))}
                </Box>
              </Box>
              <Typography 
                variant="h6" 
                gutterBottom 
                sx={{ 
                  color: '#1a1a1a',
                '&::after': {
                  content: '""',
                  position: 'absolute',
                  bottom: '15px',
                  right: '50%',
                  transform: 'translateX(50%)',
                  width: '45px',
                  height: '45px',
                  background: '#222',
                  borderRadius: '50%',
                  border: '2px solid #444',
                  boxShadow: 'inset 0 0 10px rgba(0,0,0,0.8)',
                  '&:hover': {
                    background: '#2a2a2a'
                  }
                }
                }}
              >
                Дом
              </Typography>
            </Paper>
      </Box>

      <Box sx={{ display: 'flex', flexDirection: { xs: 'column', md: 'row' }, gap: 4 }}>
        <Box 
          component={motion.div}
          initial={{ x: -50, opacity: 0 }}
          animate={{ x: 0, opacity: 1 }}
          transition={{ duration: 0.5, delay: 0.2 }}
          sx={{ flex: 1, minHeight: '731px' }}
        >
          <Typography 
            variant="h5" 
            gutterBottom 
            sx={{ 
              fontWeight: 'bold',
              color: '#9C27B0',
              textTransform: 'uppercase',
              borderLeft: '4px solid #9C27B0',
              pl: 2
            }}
          >
            Создать карточку
          </Typography>
          <Card 
            component={motion.div}
            whileHover={{ boxShadow: '0 10px 20px rgba(0,0,0,0.2)' }}
            sx={{ 
              borderRadius: '16px',
              overflow: 'hidden',
              boxShadow: '0 4px 12px rgba(0,0,0,0.15)'
            }}
          >
            <CardContent sx={{ p: 3 }}>
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                <Box 
                  sx={{ 
                    display: 'flex', 
                    justifyContent: 'center', 
                    mb: 2,
                    backgroundColor: '#f5f5f5',
                    borderRadius: '12px',
                    py: 1
                  }}
                >
                  {['Б', 'И', 'Н', 'Г', 'О'].map((letter, i) => (
                    <Typography
                      key={letter}
                      variant="h5"
                      component={motion.div}
                      whileHover={{ scale: 1.1, rotate: 5 }}
                      sx={{
                        width: '80px',
                        textAlign: 'center',
                        fontWeight: 'bold',
                        color: BINGO_COLORS[i],
                        textShadow: '1px 1px 2px rgba(0,0,0,0.2)'
                      }}
                    >
                      {letter}
                    </Typography>
                  ))}
                </Box>
                {currentNumbers.map((row, i) => (
                  <Box key={i} display="flex" justifyContent="center" width="100%">
                    {row.map((num, j) => (
                      <TextField
                        key={j}
                        value={i === 2 && j === 2 ? '*' : num}
                        onChange={(e) => handleNumberChange(i, j, e.target.value)}
                        disabled={i === 2 && j === 2}
                        error={validationErrors[`${i}-${j}`]}
                        sx={{
                          width: '80px',
                          height: '80px',
                          m: 0.5,
                          '& .MuiInputBase-root': {
                            borderRadius: '12px',
                            backgroundColor: i === 2 && j === 2 ? '#FFC107' : 'white',
                            transition: 'all 0.3s ease',
                            '&:hover': {
                              transform: 'translateY(-2px)',
                              boxShadow: '0 4px 8px rgba(0,0,0,0.1)'
                            }
                          },
                          '& .MuiInputBase-input': {
                            textAlign: 'center',
                            fontSize: '1.5rem',
                            padding: '0.5rem',
                            height: '60px',
                            fontWeight: 'bold',
                            color: i === 2 && j === 2 ? '#7B1FA2' : BINGO_COLORS[j],
                            '&::-webkit-inner-spin-button, &::-webkit-outer-spin-button': {
                              display: 'none',
                            },
                          },
                        }}
                        inputProps={{
                          style: { textAlign: 'center' },
                          maxLength: 2
                        }}
                      />
                    ))}
                  </Box>
                ))}
              </Box>

              <Box sx={{ mt: 2, display: 'flex', alignItems: 'center', gap: 2, justifyContent: 'center' }}>
                <FileInput
                  id="background-upload"
                  onChange={handleFileSelect}
                />
                <label htmlFor="background-upload">
                  <Button
                    variant="outlined"
                    component="span"
                    startIcon={<ImageIcon />}
                    sx={{ borderRadius: '12px' }}
                  >
                    Выбрать фон
                  </Button>
                </label>
                {previewImage && (
                  <Box
                    component="img"
                    src={previewImage}
                    sx={{
                      width: 50,
                      height: 50,
                      objectFit: 'cover',
                      borderRadius: '8px'
                    }}
                  />
                )}
              </Box>

              <Box sx={{ mt: 2, display: 'flex', alignItems: 'center', gap: 2, justifyContent: 'center' }}>
                <Button
                  variant="outlined"
                  component="span"
                  startIcon={<AutoFixHighIcon />}
                  onClick={() => {
                    setSelectedCardId(null);
                    setMagicMantras([]);
                    setMantraDialogOpen(true);
                  }}
                  sx={{ borderRadius: '12px' }}
                >
                  Добавить мантры
                </Button>
                {magicMantras.length > 0 && (
                  <Chip 
                    label={`${magicMantras.length} мантр`} 
                    color="secondary" 
                    variant="outlined" 
                  />
                )}
              </Box>

              <Box sx={{ display: 'flex', gap: 2, mt: 3 }}>
                <Button
                  variant="outlined"
                  color="secondary"
                  onClick={generateRandomCard}
                  component={motion.button}
                  whileHover={{ scale: 1.03 }}
                  whileTap={{ scale: 0.97 }}
                  startIcon={<CasinoIcon />}
                  sx={{ 
                    flex: 1,
                    py: 1.5,
                    borderRadius: '12px',
                    borderWidth: '2px'
                  }}
                >
                  Заполнить случайно
                </Button>
                <Button
                  variant="contained"
                  color="primary"
                  onClick={handleCreateCard}
                  component={motion.button}
                  whileHover={{ scale: 1.03 }}
                  whileTap={{ scale: 0.97 }}
                  startIcon={<AddCircleIcon />}
                  disabled={cards.length >= 1}
                  sx={{ 
                    flex: 1,
                    py: 1.5,
                    borderRadius: '12px',
                    boxShadow: '0 4px 8px rgba(0,0,0,0.2)'
                  }}
                >
                  Создать карточку
                </Button>
              </Box>
            </CardContent>
          </Card>
        </Box>

        <Box 
          component={motion.div}
          initial={{ x: 50, opacity: 0 }}
          animate={{ x: 0, opacity: 1 }}
          transition={{ duration: 0.5, delay: 0.4 }}
          sx={{ flex: 1, minHeight: '731px' }}
        >
          <Typography 
            variant="h5" 
            gutterBottom 
            sx={{ 
              fontWeight: 'bold',
              color: '#00FF10',
              textTransform: 'uppercase',
              borderLeft: '4px solid #00FF10',
              pl: 2
            }}
          >
            Ваша карточка
          </Typography>
          <Box sx={{
            '@keyframes highlight': {
              '0%': {
                transform: 'scale(1)',
                boxShadow: '0 0 0 rgba(156, 39, 176, 0)'
              },
              '50%': {
                transform: 'scale(1.1)',
                boxShadow: '0 0 20px rgba(156, 39, 176, 0.8)'
              },
              '100%': {
                transform: 'scale(1)',
                boxShadow: '0 0 10px rgba(156, 39, 176, 0.5)'
              }
            },
            minHeight: '731px'
          }}>
            <AnimatePresence>
              {cards.map((card) => (
                <motion.div
                  key={card.id}
                  layout
                  initial={{ opacity: 0, y: 20 }}
                  animate={{ 
                    opacity: 1, 
                    y: 0,
                    scale: card.isNew ? [0.8, 1] : 1,
                    ...(winningCard === card.id && {
                      y: [0, -10, 0],
                      transition: {
                        y: {
                          repeat: Infinity,
                          duration: 1,
                          repeatType: "loop"
                        }
                      }
                    })
                  }}
                  exit={{ opacity: 0, y: -20 }}
                  transition={{ 
                    duration: 0.3,
                    type: "spring",
                    stiffness: 200,
                    damping: 20
                  }}
                  style={{
                    minHeight: '731px'
                  }}
                >
                  <Paper
                    sx={{
                      p: 3,
                      mb: 3,
                      borderRadius: '16px',
                      overflow: 'hidden',
                      minHeight: '731px',
                      bgcolor: card.is_completed ? 'success.light' : 'background.paper',
                      boxShadow: card.is_completed ? 
                        '0 0 20px rgba(76, 175, 80, 0.5)' : 
                        card.isNew ? '0 0 25px rgba(255, 193, 7, 0.8)' : '0 4px 12px rgba(0,0,0,0.15)',
                      position: 'relative',
                      ...(winningCard === card.id && {
                        boxShadow: '0 0 30px rgba(76, 175, 80, 0.8)',
                        border: '2px solid #4CAF50',
                        animation: 'pulse 1.5s infinite'
                      }),
                      backgroundImage: card.background_image ? `url(${card.background_image})` : 'none',
                      backgroundSize: 'cover',
                      backgroundPosition: 'center',
                      '&::before': {
                        content: '""',
                        position: 'absolute',
                        top: 0,
                        left: 0,
                        right: 0,
                        bottom: 0,
                        backgroundColor: 'rgba(255, 255, 255, 0.9)',
                        zIndex: 0
                      }
                    }}
                  >
                    <Box sx={{ position: 'relative', zIndex: 1 }}>
                      <Box sx={{ 
                        display: 'flex', 
                        flexDirection: 'column', 
                        gap: 1,
                        minHeight: '633px'
                      }}>
                        <Box 
                          sx={{ 
                            display: 'flex', 
                            justifyContent: 'center', 
                            mb: 1,
                            backgroundColor: '#f5f5f5',
                            borderRadius: '12px',
                            py: 1
                          }}
                        >
                          {['Б', 'И', 'Н', 'Г', 'О'].map((letter, i) => (
                            <Typography
                              key={letter}
                              variant="h6"
                              sx={{
                                width: '80px',
                                textAlign: 'center',
                                fontWeight: 'bold',
                                color: BINGO_COLORS[i]
                              }}
                            >
                              {letter}
                            </Typography>
                          ))}
                        </Box>
                        {card.numbers.map((row, i) => (
                          <Box key={i} display="flex" justifyContent="center" width="100%">
                            {row.map((num, j) => (
                              <motion.div
                                key={j}
                                whileHover={{ scale: 1.05 }}
                                whileTap={{ scale: 0.95 }}
                              >
                                <Paper
                                  elevation={3}
                                  sx={{
                                    width: '80px',
                                    height: '80px',
                                    m: 0.5,
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center',
                                    fontSize: '1.5rem',
                                    fontWeight: 'bold',
                                    color: num === '*' ? '#7B1FA2' : BINGO_COLORS[j],
                                    bgcolor: (card.id === activeCard && highlightedNumbers.includes(num as number))
                                      ? 'secondary.light'
                                      : num === '*' ? '#FFF9C4' : 'background.paper',
                                    transition: 'all 0.3s ease',
                                    borderRadius: '12px',
                                    boxShadow: (card.id === activeCard && highlightedNumbers.includes(num as number))
                                      ? '0 0 10px rgba(156, 39, 176, 0.5)'
                                      : '0 2px 6px rgba(0,0,0,0.1)',
                                    border: num === '*' ? '2px dashed #FFC107' : 'none',
                                    animation: (card.id === activeCard && highlightedNumbers.includes(num as number))
                                      ? 'highlight 0.5s ease'
                                      : 'none'
                                  }}
                                >
                                  {num}
                                </Paper>
                              </motion.div>
                            ))}
                          </Box>
                        ))}
                      </Box>
                      {!card.is_played && (
                        <Box sx={{ display: 'flex', gap: 2, mt: 2 }}>
                          <FileInput
                            id={`background-update-${card.id}`}
                            onChange={handleCardBackgroundChange(card.id)}
                          />
                          <label htmlFor={`background-update-${card.id}`}>
                            <IconButton component="span" color="primary">
                              <ImageIcon />
                            </IconButton>
                          </label>
                          <IconButton 
                            color="secondary" 
                            onClick={() => handleOpenMantraDialog(card.id)}
                          >
                            <AutoFixHighIcon />
                          </IconButton>
                          {card.magic_mantras && card.magic_mantras.length > 0 && (
                            <Chip 
                              label={`${card.magic_mantras.length} мантр`} 
                              color="secondary" 
                              variant="outlined" 
                            />
                          )}
                        </Box>
                      )}
                    </Box>
                    {!card.is_completed && !card.is_played && (
                      <Button
                        fullWidth
                        variant="contained"
                        color="secondary"
                        onClick={() => handlePlayCard(card.id)}
                        component={motion.button}
                        whileHover={{ scale: 1.03 }}
                        whileTap={{ scale: 0.97 }}
                        startIcon={<PlayArrowIcon />}
                        disabled={isRevealing && activeCard !== card.id}
                        sx={{ 
                          mt: 'auto',
                          py: 1.5,
                          borderRadius: '12px',
                          fontWeight: 'bold',
                          boxShadow: '0 4px 8px rgba(0,0,0,0.2)',
                          opacity: 1
                        }}
                      >
                        {isRevealing && activeCard === card.id ? 'Показываем числа...' : 'Играть с карточкой'}
                      </Button>
                    )}
                    {card.winning_numbers && (
                      <Button
                        fullWidth
                        variant="contained"
                        color="primary"
                        onClick={() => handleApplyCard(card.id)}
                        component={motion.button}
                        whileHover={{ scale: 1.03 }}
                        whileTap={{ scale: 0.97 }}
                        startIcon={<CheckCircleIcon />}
                        disabled={isRevealing}
                        sx={{ 
                          mt: 'auto',
                          py: 1.5,
                          borderRadius: '12px',
                          fontWeight: 'bold',
                          boxShadow: '0 4px 8px rgba(0,0,0,0.2)'
                        }}
                      >
                        Сдать карточку
                      </Button>
                    )}
                  </Paper>
                </motion.div>
              ))}
            </AnimatePresence>
          </Box>
        </Box>
      </Box>

      <Dialog 
        open={mantraDialogOpen} 
        onClose={handleCloseMantraDialog}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <AutoFixHighIcon color="secondary" />
            <Typography variant="h6">Мантры на удачу</Typography>
          </Box>
        </DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', gap: 1, mb: 2, mt: 1 }}>
            <TextField
              fullWidth
              label="Новая мантра"
              value={newMantra}
              onChange={(e) => setNewMantra(e.target.value)}
              variant="outlined"
              size="small"
            />
            <Button 
              variant="contained" 
              color="secondary" 
              onClick={handleAddMantra}
              startIcon={<AddIcon />}
            >
              {editingMantraIndex !== null ? 'Изменить' : 'Добавить'}
            </Button>
          </Box>
          <Divider sx={{ my: 2 }} />
          <List>
            {magicMantras.length > 0 ? (
              magicMantras.map((mantra, index) => (
                <ListItem key={index} divider>
                  <ListItemText primary={mantra} />
                  <ListItemSecondaryAction>
                    <IconButton 
                      edge="end" 
                      aria-label="edit" 
                      onClick={() => handleEditMantra(index)}
                      sx={{ mr: 1 }}
                    >
                      <EditIcon />
                    </IconButton>
                    <IconButton 
                      edge="end" 
                      aria-label="delete" 
                      onClick={() => handleDeleteMantra(index)}
                    >
                      <DeleteIcon />
                    </IconButton>
                  </ListItemSecondaryAction>
                </ListItem>
              ))
            ) : (
              <Typography variant="body2" color="text.secondary" sx={{ textAlign: 'center', py: 2 }}>
                Нет мантр. Добавьте мантру на карточку, чтобы призвать удачу!
              </Typography>
            )}
          </List>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseMantraDialog}>Отмена</Button>
          <Button onClick={handleSaveMantras} variant="contained" color="primary">
            Сохранить
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default Game; 
