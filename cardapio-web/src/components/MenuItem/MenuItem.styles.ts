import styled, { keyframes } from 'styled-components';
import { theme } from '../../styles/theme';

const fadeIn = keyframes`
  from { opacity: 0; }
  to { opacity: 1; }
`;

const sparkle = keyframes`
  0%, 100% { transform: scale(1) rotate(0deg); }
  50% { transform: scale(1.2) rotate(180deg); }
`;

export const MenuItemCard = styled.div<{ $isSpecial?: boolean }>`
  background: ${props => props.$isSpecial
    ? theme.gradients.primary
    : theme.colors.white};
  border-radius: 20px;
  padding: 30px;
  box-shadow: 0 10px 40px rgba(0,0,0,0.08);
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  position: relative;
  overflow: hidden;
  cursor: pointer;
  animation: ${fadeIn} 0.6s ease-out backwards;
  color: ${props => props.$isSpecial ? theme.colors.white : theme.colors.dark};

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 5px;
    height: 100%;
    background: ${props => props.$isSpecial
      ? theme.colors.secondary
      : theme.gradients.secondary};
    transition: width 0.3s ease;
  }

  &:hover::before {
    width: 100%;
    opacity: 0.1;
  }

  &:hover {
    transform: translateY(-10px) scale(1.02);
    box-shadow: 0 20px 60px rgba(0,0,0,0.15);
  }

  ${props => props.$isSpecial && `
    &::after {
      content: '⭐';
      position: absolute;
      top: 15px;
      right: 15px;
      font-size: 1.5em;
      animation: ${sparkle} 2s ease-in-out infinite;
    }
  `}
`;

export const ItemHeader = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
`;

export const ItemName = styled.h3<{ $isSpecial?: boolean }>`
  font-size: 1.4em;
  font-weight: 700;
  color: ${props => props.$isSpecial ? theme.colors.white : theme.colors.dark};
  margin-bottom: 5px;
  transition: color 0.3s ease;

  ${MenuItemCard}:hover & {
    color: ${props => props.$isSpecial ? theme.colors.secondary : theme.colors.primary};
  }
`;

export const ItemPrice = styled.span`
  font-size: 1.6em;
  font-weight: 800;
  background: ${theme.gradients.secondary};
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  white-space: nowrap;
`;

export const ItemDescription = styled.p<{ $isSpecial?: boolean }>`
  font-size: 0.95em;
  color: ${props => props.$isSpecial
    ? 'rgba(255,255,255,0.8)'
    : theme.colors.gray};
  line-height: 1.5;
  font-style: italic;
`;