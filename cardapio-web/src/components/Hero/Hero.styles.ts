import styled, { keyframes } from 'styled-components';
import { theme } from '../../styles/theme';

const float = keyframes`
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-30px) rotate(10deg); }
`;

const heroEntrance = keyframes`
  from {
    opacity: 0;
    transform: translateY(50px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
`;

const glow = keyframes`
  0%, 100% { filter: brightness(1); }
  50% { filter: brightness(1.2); }
`;

const pulse = keyframes`
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
`;

const bounce = keyframes`
  0%, 100% { transform: translateX(-50%) translateY(0); }
  50% { transform: translateX(-50%) translateY(10px); }
`;

export const HeroContainer = styled.section`
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: ${theme.gradients.primary};
  overflow: hidden;

  &::before {
    content: '🍖';
    position: absolute;
    font-size: 300px;
    opacity: 0.03;
    top: -50px;
    right: -100px;
    animation: ${float} 6s ease-in-out infinite;
  }

  &::after {
    content: '🍺';
    position: absolute;
    font-size: 250px;
    opacity: 0.03;
    bottom: -50px;
    left: -80px;
    animation: ${float} 8s ease-in-out infinite reverse;
  }
`;

export const HeroContent = styled.div`
  text-align: center;
  color: white;
  z-index: 10;
  padding: 40px;
  animation: ${heroEntrance} 1s ease-out;
`;

export const Logo = styled.h1`
  font-family: ${theme.fonts.bebas};
  font-size: 5em;
  letter-spacing: 3px;
  margin-bottom: 20px;
  text-shadow: 4px 4px 8px rgba(0,0,0,0.3);
  background: ${theme.gradients.secondary};
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: ${glow} 3s ease-in-out infinite;

  @media (max-width: ${theme.breakpoints.mobile}) {
    font-size: 3.5em;
  }
`;

export const Tagline = styled.p`
  font-size: 1.5em;
  opacity: 0.9;
  margin-bottom: 30px;
  font-weight: 300;
  letter-spacing: 2px;

  @media (max-width: ${theme.breakpoints.mobile}) {
    font-size: 1.2em;
  }
`;

export const CTAButton = styled.a`
  display: inline-block;
  padding: 15px 40px;
  background: ${theme.gradients.secondary};
  color: white;
  text-decoration: none;
  border-radius: 50px;
  font-weight: 600;
  font-size: 1.1em;
  transition: all 0.3s ease;
  box-shadow: 0 10px 30px rgba(244, 160, 32, 0.3);
  animation: ${pulse} 2s infinite;

  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 15px 40px rgba(244, 160, 32, 0.4);
  }
`;

export const ScrollIndicator = styled.div`
  position: absolute;
  bottom: 30px;
  left: 50%;
  transform: translateX(-50%);
  animation: ${bounce} 2s infinite;

  &::after {
    content: '↓';
    font-size: 2em;
    color: rgba(255,255,255,0.5);
  }
`;