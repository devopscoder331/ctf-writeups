import React, { useState } from 'react';
import {
  Box,
  Container,
  Typography,
  TextField,
  Button,
  Paper,
  Alert,
  InputAdornment,
  Fade,
} from '@mui/material';
import CreditCardIcon from '@mui/icons-material/CreditCard';
import LockIcon from '@mui/icons-material/Lock';
import EventIcon from '@mui/icons-material/Event';
import PetsIcon from '@mui/icons-material/Pets';
import EmailIcon from '@mui/icons-material/Email';

function PaymentPage({ onSuccess, onCancel, selectedPackage }) {
  const [cardData, setCardData] = useState({
    number: '',
    expiry: '',
    cvv: '',
    name: '',
    email: ''
  });

  const [error, setError] = useState('');
  const [isProcessing, setIsProcessing] = useState(false);
  const [showCapyMessage, setShowCapyMessage] = useState(false);

  const handleInputChange = (field) => (event) => {
    let value = event.target.value;
    
    if (field === 'number') {
      value = value.replace(/\s/g, '').match(/.{1,4}/g)?.join(' ') || '';
      setShowCapyMessage(false);
    }
    if (field === 'expiry') {
      value = value.replace(/\D/g, '').match(/.{1,2}/g)?.join('/') || '';
    }
    
    setCardData(prev => ({
      ...prev,
      [field]: value
    }));
    setError('');
  };

  const validateCapyBank = (cardNumber) => {
    const number = cardNumber.replace(/\s/g, '');
    if (number.startsWith('777')) {
      setShowCapyMessage(true);
      return false;
    }
    return false;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsProcessing(true);
    setError('');

    if (!cardData.email || !cardData.number || !cardData.expiry || !cardData.cvv || !cardData.name) {
      setError('Пожалуйста, заполните все поля');
      setIsProcessing(false);
      return;
    }

    if (!cardData.email.match(/^[^\s@]+@[^\s@]+\.[^\s@]+$/)) {
      setError('Пожалуйста, введите корректный email адрес');
      setIsProcessing(false);
      return;
    }

    setShowCapyMessage(true);
    setIsProcessing(false);
    setError('К оплате принимаются только карты Капибанка');
  };

  return (
    <Box sx={{ 
      minHeight: '100vh', 
      bgcolor: '#1a1a1a',
      py: 4,
      px: 2
    }}>
      <Container maxWidth="sm">
        <Paper 
          elevation={3}
          sx={{
            p: 4,
            bgcolor: 'rgba(42, 8, 69, 0.95)',
            backdropFilter: 'blur(10px)',
            borderRadius: 4,
            border: '1px solid rgba(78, 205, 196, 0.1)',
          }}
        >
          <Box sx={{ mb: 4, textAlign: 'center' }}>
            <Typography variant="h4" gutterBottom sx={{ 
              color: '#4ECDC4',
              textShadow: '0 0 10px rgba(78, 205, 196, 0.3)'
            }}>
              Оплата
            </Typography>
            {selectedPackage && (
              <Typography variant="h6" sx={{ color: 'rgba(255,255,255,0.9)' }}>
                {selectedPackage.amount} {selectedPackage.amount >= 5 ? 'Астролапок' : 'Астролапки'} за {selectedPackage.price}
              </Typography>
            )}
          </Box>

          <form onSubmit={handleSubmit}>
            <TextField
              fullWidth
              label="Email"
              value={cardData.email}
              onChange={handleInputChange('email')}
              margin="normal"
              type="email"
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <EmailIcon sx={{ color: '#4ECDC4' }} />
                  </InputAdornment>
                ),
              }}
              placeholder="example@mail.com"
              sx={{
                '& .MuiOutlinedInput-root': {
                  '& fieldset': {
                    borderColor: 'rgba(78, 205, 196, 0.3)',
                  },
                  '&:hover fieldset': {
                    borderColor: 'rgba(78, 205, 196, 0.5)',
                  },
                  '&.Mui-focused fieldset': {
                    borderColor: '#4ECDC4',
                  },
                },
                '& .MuiInputLabel-root': {
                  color: 'rgba(255, 255, 255, 0.7)',
                },
                '& .MuiOutlinedInput-input': {
                  color: 'rgba(255, 255, 255, 0.9)',
                },
              }}
            />

            <TextField
              fullWidth
              label="Номер карты"
              value={cardData.number}
              onChange={handleInputChange('number')}
              margin="normal"
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <CreditCardIcon sx={{ color: '#4ECDC4' }} />
                  </InputAdornment>
                ),
              }}
              placeholder="Введите номер карты"
              inputProps={{ maxLength: 19 }}
              sx={{
                '& .MuiOutlinedInput-root': {
                  '& fieldset': {
                    borderColor: 'rgba(78, 205, 196, 0.3)',
                  },
                  '&:hover fieldset': {
                    borderColor: 'rgba(78, 205, 196, 0.5)',
                  },
                  '&.Mui-focused fieldset': {
                    borderColor: '#4ECDC4',
                  },
                },
                '& .MuiInputLabel-root': {
                  color: 'rgba(255, 255, 255, 0.7)',
                },
                '& .MuiOutlinedInput-input': {
                  color: 'rgba(255, 255, 255, 0.9)',
                },
              }}
            />

            <Box sx={{ display: 'flex', gap: 2, my: 2 }}>
              <TextField
                label="Срок действия"
                value={cardData.expiry}
                onChange={handleInputChange('expiry')}
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <EventIcon sx={{ color: '#4ECDC4' }} />
                    </InputAdornment>
                  ),
                }}
                placeholder="MM/YY"
                inputProps={{ maxLength: 5 }}
                sx={{
                  flex: 1,
                  '& .MuiOutlinedInput-root': {
                    '& fieldset': {
                      borderColor: 'rgba(78, 205, 196, 0.3)',
                    },
                    '&:hover fieldset': {
                      borderColor: 'rgba(78, 205, 196, 0.5)',
                    },
                    '&.Mui-focused fieldset': {
                      borderColor: '#4ECDC4',
                    },
                  },
                  '& .MuiInputLabel-root': {
                    color: 'rgba(255, 255, 255, 0.7)',
                  },
                  '& .MuiOutlinedInput-input': {
                    color: 'rgba(255, 255, 255, 0.9)',
                  },
                }}
              />
              <TextField
                label="CVV"
                value={cardData.cvv}
                onChange={handleInputChange('cvv')}
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <LockIcon sx={{ color: '#4ECDC4' }} />
                    </InputAdornment>
                  ),
                }}
                type="password"
                inputProps={{ maxLength: 3 }}
                sx={{
                  width: '120px',
                  '& .MuiOutlinedInput-root': {
                    '& fieldset': {
                      borderColor: 'rgba(78, 205, 196, 0.3)',
                    },
                    '&:hover fieldset': {
                      borderColor: 'rgba(78, 205, 196, 0.5)',
                    },
                    '&.Mui-focused fieldset': {
                      borderColor: '#4ECDC4',
                    },
                  },
                  '& .MuiInputLabel-root': {
                    color: 'rgba(255, 255, 255, 0.7)',
                  },
                  '& .MuiOutlinedInput-input': {
                    color: 'rgba(255, 255, 255, 0.9)',
                  },
                }}
              />
            </Box>

            <TextField
              fullWidth
              label="Имя владельца"
              value={cardData.name}
              onChange={handleInputChange('name')}
              margin="normal"
              placeholder="Capy Bara"
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <PetsIcon sx={{ color: '#4ECDC4' }} />
                  </InputAdornment>
                ),
              }}
              sx={{
                '& .MuiOutlinedInput-root': {
                  '& fieldset': {
                    borderColor: 'rgba(78, 205, 196, 0.3)',
                  },
                  '&:hover fieldset': {
                    borderColor: 'rgba(78, 205, 196, 0.5)',
                  },
                  '&.Mui-focused fieldset': {
                    borderColor: '#4ECDC4',
                  },
                },
                '& .MuiInputLabel-root': {
                  color: 'rgba(255, 255, 255, 0.7)',
                },
                '& .MuiOutlinedInput-input': {
                  color: 'rgba(255, 255, 255, 0.9)',
                },
              }}
            />

            {error && (
              <Fade in>
                <Alert 
                  severity={showCapyMessage ? "info" : "error"}
                  sx={{ 
                    my: 2,
                    bgcolor: showCapyMessage 
                      ? 'rgba(78, 205, 196, 0.1)' 
                      : 'rgba(255, 107, 107, 0.1)',
                    color: showCapyMessage ? '#4ECDC4' : '#FF6B6B',
                    border: `1px solid ${showCapyMessage ? '#4ECDC4' : '#FF6B6B'}`,
                    '& .MuiAlert-icon': {
                      color: showCapyMessage ? '#4ECDC4' : '#FF6B6B'
                    }
                  }}
                >
                  {error}
                </Alert>
              </Fade>
            )}

            <Box sx={{ display: 'flex', gap: 2, mt: 4 }}>
              <Button
                variant="outlined"
                onClick={onCancel}
                fullWidth
                sx={{
                  borderColor: '#4ECDC4',
                  color: '#4ECDC4',
                  '&:hover': {
                    borderColor: '#3dbdb5',
                    bgcolor: 'rgba(77, 205, 196, 0.1)'
                  }
                }}
              >
                Отмена
              </Button>
              <Button
                type="submit"
                variant="contained"
                fullWidth
                disabled={isProcessing}
                sx={{
                  bgcolor: '#4ECDC4',
                  color: '#1a1a1a',
                  fontWeight: 'bold',
                  '&:hover': {
                    bgcolor: '#3dbdb5'
                  },
                  '&:disabled': {
                    bgcolor: 'rgba(78, 205, 196, 0.3)',
                    color: 'rgba(255, 255, 255, 0.3)'
                  }
                }}
              >
                {isProcessing ? 'Обработка...' : 'Оплатить'}
              </Button>
            </Box>
          </form>

          <Typography 
            variant="body2" 
            sx={{ 
              mt: 3, 
              textAlign: 'center',
              color: 'rgba(255,255,255,0.6)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: 1
            }}
          >
            <LockIcon sx={{ fontSize: '1rem', color: '#4ECDC4' }} />
            Безопасная оплата через КапиБанк
          </Typography>
        </Paper>
      </Container>
    </Box>
  );
}

export default PaymentPage; 