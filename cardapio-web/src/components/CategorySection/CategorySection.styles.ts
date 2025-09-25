import styled, { keyframes } from 'styled-components';
import { theme } from '../../styles/theme';

const fadeInUp = keyframes`
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
`;

export const CategoryContainer = styled.section<{ $background?: string }>`
  padding: 80px 20px;
  position: relative;
  background: ${props => props.$background || 'transparent'};

  &:nth-child(even) {
    background: rgba(255,255,255,0.5);
    backdrop-filter: blur(10px);
  }
`;

export const CategoryHeader = styled.div`
  text-align: center;
  margin-bottom: 60px;
  animation: ${fadeInUp} 0.8s ease-out;
`;

export const CategoryTitle = styled.h2`
  font-family: ${theme.fonts.bebas};
  font-size: 4em;
  letter-spacing: 2px;
  color: ${theme.colors.primary};
  margin-bottom: 10px;
  position: relative;
  display: inline-block;

  &::after {
    content: '';
    position: absolute;
    bottom: -10px;
    left: 50%;
    transform: translateX(-50%);
    width: 100px;
    height: 4px;
    background: linear-gradient(90deg, transparent, ${theme.colors.secondary}, transparent);
    border-radius: 2px;
  }

  @media (max-width: ${theme.breakpoints.mobile}) {
    font-size: 3em;
  }
`;

export const CategorySubtitle = styled.p`
  color: ${theme.colors.gray};
  font-size: 1.2em;
  font-weight: 300;
  margin-top: 20px;
`;

export const MenuGrid = styled.div`
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 30px;
  max-width: 1200px;
  margin: 0 auto;

  @media (max-width: ${theme.breakpoints.mobile}) {
    grid-template-columns: 1fr;
    gap: 20px;
  }
`;