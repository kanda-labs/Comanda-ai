import styled from 'styled-components';
import { theme } from '../../styles/theme';

export const ChoppSectionContainer = styled.section`
  padding: 80px 20px;
  background: linear-gradient(135deg, rgba(244, 160, 32, 0.1) 0%, rgba(26, 95, 63, 0.1) 100%);
  position: relative;
`;

export const ChoppHeader = styled.div`
  text-align: center;
  margin-bottom: 60px;
`;

export const ChoppTitle = styled.h2`
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

export const ChoppSubtitle = styled.p`
  color: ${theme.colors.gray};
  font-size: 1.2em;
  font-weight: 300;
  margin-top: 20px;

  &::before {
    content: '🍺 ';
    font-size: 1.2em;
  }
`;