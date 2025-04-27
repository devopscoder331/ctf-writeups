import React from 'react';
import {
  Box,
  Typography,
  Paper,
  List,
  ListItem,
  ListItemText,
  Button,
  Container,
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import MenuBookIcon from '@mui/icons-material/MenuBook';

const Rules: React.FC = () => {
  const navigate = useNavigate();

  return (
    <Container maxWidth="lg" sx={{ pb: 5 }}>
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
          Правила игры
        </Typography>
        <Button
          variant="contained"
          color="secondary"
          onClick={() => navigate('/game')}
          component={motion.button}
          whileHover={{ scale: 1.05 }}
          whileTap={{ scale: 0.95 }}
          startIcon={<ArrowBackIcon />}
          sx={{ 
            borderRadius: '20px',
            px: 3
          }}
        >
          Вернуться к игре
        </Button>
      </Box>

      <Box 
        component={motion.div}
        initial={{ y: 20, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        transition={{ duration: 0.5, delay: 0.2 }}
      >
        <Paper 
          sx={{ 
            p: 4,
            borderRadius: '16px',
            background: 'linear-gradient(145deg, #ffffff 0%, #f5f5f5 100%)',
            boxShadow: '0 8px 32px rgba(0,0,0,0.1)',
            border: '1px solid rgba(255,255,255,0.2)',
            backdropFilter: 'blur(10px)',
          }}
        >
          <Box 
            sx={{ 
              display: 'flex',
              alignItems: 'center',
              gap: 2,
              mb: 3,
              pb: 2,
              borderBottom: '2px solid rgba(76, 175, 80, 0.2)'
            }}
          >
            <MenuBookIcon 
              sx={{ 
                fontSize: 40,
                color: '#4CAF50'
              }} 
            />
            <Typography 
              variant="h4" 
              sx={{ 
                fontWeight: 'bold',
                background: 'linear-gradient(45deg, #4CAF50 30%, #81C784 90%)',
                WebkitBackgroundClip: 'text',
                WebkitTextFillColor: 'transparent'
              }}
            >
              Как играть
            </Typography>
          </Box>

          <List sx={{ '& .MuiListItem-root': { mb: 2 } }}>
            <motion.div
              initial={{ x: -20, opacity: 0 }}
              animate={{ x: 0, opacity: 1 }}
              transition={{ delay: 0.3 }}
            >
              <ListItem 
                component={Paper} 
                sx={{ 
                  p: 2,
                  borderRadius: '12px',
                  boxShadow: '0 4px 12px rgba(0,0,0,0.05)',
                  transition: 'transform 0.2s, box-shadow 0.2s'
                }}
              >
                <ListItemText
                  primary={
                    <Typography variant="h6" color="primary" gutterBottom>
                      Создание карточки
                    </Typography>
                  }
                  secondary={
                    <Typography variant="body1" color="text.secondary">
                      Каждая карточка бинго имеет 25 квадратов, расположенных в сетке 5x5. Центральный квадрат считается автоматически выигрышным.
                    </Typography>
                  }
                />
              </ListItem>
            </motion.div>

            <motion.div
              initial={{ x: -20, opacity: 0 }}
              animate={{ x: 0, opacity: 1 }}
              transition={{ delay: 0.4 }}
            >
              <ListItem 
                component={Paper} 
                sx={{ 
                  p: 2,
                  borderRadius: '12px',
                  boxShadow: '0 4px 12px rgba(0,0,0,0.05)',
                  transition: 'transform 0.2s, box-shadow 0.2s'
                }}
              >
                <ListItemText
                  primary={
                    <Typography variant="h6" color="primary" gutterBottom>
                      Правила нумерации
                    </Typography>
                  }
                  secondary={
                    <Box sx={{ 
                      mt: 1,
                      '& p': {
                        m: 0,
                        py: 0.5,
                        px: 2,
                        borderRadius: '8px',
                        transition: 'background-color 0.2s',
                        '&:hover': {
                          bgcolor: 'rgba(76, 175, 80, 0.1)'
                        }
                      }
                    }}>
                      <p>• колонка Б: числа 1-15</p>
                      <p>• колонка И: числа 16-30</p>
                      <p>• колонка Н: числа 31-45 (середина *)</p>
                      <p>• колонка Г: числа 46-60</p>
                      <p>• колонка О: числа 61-75</p>
                    </Box>
                  }
                />
              </ListItem>
            </motion.div>

            <motion.div
              initial={{ x: -20, opacity: 0 }}
              animate={{ x: 0, opacity: 1 }}
              transition={{ delay: 0.5 }}
            >
              <ListItem 
                component={Paper} 
                sx={{ 
                  p: 2,
                  borderRadius: '12px',
                  boxShadow: '0 4px 12px rgba(0,0,0,0.05)',
                  transition: 'transform 0.2s, box-shadow 0.2s'
                }}
              >
                <ListItemText
                  primary={
                    <Typography variant="h6" color="primary" gutterBottom>
                      Создание карточки
                    </Typography>
                  }
                  secondary={
                    <Typography variant="body1" color="text.secondary">
                      Вы можете либо вручную ввести числа, следуя правилам выше, либо использовать кнопку 'Сгенерировать случайную карточку' для автоматического создания действительной карточки.
                    </Typography>
                  }
                />
              </ListItem>
            </motion.div>

            <motion.div
              initial={{ x: -20, opacity: 0 }}
              animate={{ x: 0, opacity: 1 }}
              transition={{ delay: 0.6 }}
            >
              <ListItem 
                component={Paper} 
                sx={{ 
                  p: 2,
                  borderRadius: '12px',
                  boxShadow: '0 4px 12px rgba(0,0,0,0.05)',
                  transition: 'transform 0.2s, box-shadow 0.2s'
                }}
              >
                <ListItemText
                  primary={
                    <Typography variant="h6" color="primary" gutterBottom>
                      Игровой процесс
                    </Typography>
                  }
                  secondary={
                    <Typography variant="body1" color="text.secondary">
                      Нажмите «Играть с карточкой», чтобы начать вытягивать числа. Числа будут подсвечиваться на вашей карточке по мере их вытягивания.
                    </Typography>
                  }
                />
              </ListItem>
            </motion.div>

            <motion.div
              initial={{ x: -20, opacity: 0 }}
              animate={{ x: 0, opacity: 1 }}
              transition={{ delay: 0.7 }}
            >
              <ListItem 
                component={Paper} 
                sx={{ 
                  p: 2,
                  borderRadius: '12px',
                  boxShadow: '0 4px 12px rgba(0,0,0,0.05)',
                  transition: 'transform 0.2s, box-shadow 0.2s'
                }}
              >
                <ListItemText
                  primary={
                    <Typography variant="h6" color="primary" gutterBottom>
                      Выигрышные комбинации
                    </Typography>
                  }
                  secondary={
                    <Typography variant="body1" color="text.secondary">
                      Вы выигрываете, заполнив любую из этих комбинаций вытянутыми числами:<br/>
                      • любая полная строка<br/>
                      • любая полная колонка<br/>
                      • любая полная диагональ<br/>
                      Центральная ячейка всегда учитывается в вашей комбинации.
                    </Typography>
                  }
                />
              </ListItem>
            </motion.div>

            <motion.div
              initial={{ x: -20, opacity: 0 }}
              animate={{ x: 0, opacity: 1 }}
              transition={{ delay: 0.8 }}
            >
              <ListItem 
                component={Paper} 
                sx={{ 
                  p: 2,
                  borderRadius: '12px',
                  boxShadow: '0 4px 12px rgba(0,0,0,0.05)',
                  transition: 'transform 0.2s, box-shadow 0.2s'
                }}
              >
                <ListItemText
                  primary={
                    <Typography variant="h6" color="primary" gutterBottom>
                      Завершение игры
                    </Typography>
                  }
                  secondary={
                    <Typography variant="body1" color="text.secondary">
                      После игры с карточкой нажмите «Сдать карточку», чтобы отметить ее как сыгранную. Каждой карточкой можно сыграть только один раз.
                    </Typography>
                  }
                />
              </ListItem>
            </motion.div>
          </List>

          <motion.div
            initial={{ y: 20, opacity: 0 }}
            animate={{ y: 0, opacity: 1 }}
            transition={{ delay: 0.9 }}
          >
            <Typography 
              variant="body2" 
              sx={{ 
                mt: 4,
                p: 2,
                borderRadius: '12px',
                bgcolor: 'rgba(76, 175, 80, 0.1)',
                border: '1px dashed #4CAF50',
                color: 'text.secondary',
                fontStyle: 'italic'
              }}
            >
              Помните: каждая карточка должна иметь уникальные числа в пределах своих диапазонов колонок. 
              Игра проверит вашу карточку, чтобы убедиться, что она соответствует всем правилам.
            </Typography>
          </motion.div>
        </Paper>
      </Box>
    </Container>
  );
};

export default Rules; 
