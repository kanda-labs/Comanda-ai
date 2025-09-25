import styled from 'styled-components';
import { theme } from '../../styles/theme';

export const FooterContainer = styled.footer`
  background: ${theme.colors.dark};
  color: white;
  padding: 60px 20px 30px;
  text-align: center;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: linear-gradient(135deg, transparent 30%, rgba(244, 160, 32, 0.1) 100%);
    pointer-events: none;
  }
`;

export const FooterContent = styled.div`
  position: relative;
  z-index: 1;
`;

export const FooterLogo = styled.h3`
  font-family: ${theme.fonts.bebas};
  font-size: 3em;
  margin-bottom: 20px;
  background: ${theme.gradients.secondary};
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
`;

export const FooterInfo = styled.p`
  font-size: 1.1em;
  opacity: 0.8;
  margin-bottom: 30px;
`;

export const SocialLinks = styled.div`
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-bottom: 30px;
`;

export const SocialLink = styled.a`
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: rgba(255,255,255,0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5em;
  transition: all 0.3s ease;
  cursor: pointer;

  &:hover {
    background: ${theme.colors.secondary};
    transform: translateY(-5px);
  }
`;

export const Copyright = styled.p`
  opacity: 0.5;
  font-size: 0.9em;
  padding-top: 20px;
  border-top: 1px solid rgba(255,255,255,0.1);
`;