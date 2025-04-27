import React from 'react';
import { AppBar, Toolbar, Typography, Button, Container, Box } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { motion } from 'framer-motion';
import BingoBall from './BingoBall';

const Layout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const navigate = useNavigate();
  const { isAuthenticated, logout } = useAuth();

  return (
    <Box sx={{ 
      minHeight: '100vh', 
      bgcolor: 'background.default', 
      backgroundImage: 'url(images/back.png)', 
      backgroundSize: 'cover', 
      backgroundPosition: 'center',
      backgroundAttachment: 'fixed',
      display: 'flex',
      flexDirection: 'column'
    }}>
      <AppBar position="static" color="primary" elevation={0}>
        <Toolbar>
          <motion.div
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.5 }}
          >
               <Box sx={{ display: 'flex', gap: 1, alignItems: 'center' }}> 
            <Typography
              variant="h6"
              component="div"
              sx={{
                flexGrow: 1,
                fontFamily: '"Poppins", sans-serif',
                fontWeight: 600,
                cursor: 'pointer'
              }}
              onClick={() => navigate('/')}
            >
              КапиБинго
            </Typography>
            
            <BingoBall value={13} size="small" />
            <BingoBall value={37} size="small" />
            </Box>

          </motion.div>
          <Box sx={{ flexGrow: 1 }} />
          {isAuthenticated ? (
            <Button color="inherit" onClick={logout}>
              Выйти
            </Button>
          ) : (
            <>
              <Button color="inherit" onClick={() => navigate('/login')}>
                Войти
              </Button>
              <Button color="inherit" onClick={() => navigate('/register')}>
                Регистрация
              </Button>
            </>
          )}
        </Toolbar>
      </AppBar>
      <Container
        component={motion.div}
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
        maxWidth="lg"
        sx={{ 
          mt: 4, 
          mb: 4,
          flex: 1,
          display: 'flex',
          flexDirection: 'column'
        }}
      >
        {children}
      </Container>
    </Box>
  );
};

export default Layout; 