import React from 'react';
import {
  HeroContainer,
  HeroContent,
  Logo,
  Tagline,
  CTAButton,
  ScrollIndicator
} from './Hero.styles';

export const Hero: React.FC = () => {
  return (
    <HeroContainer>
      <HeroContent>
        <Logo>TRAILER UÇA</Logo>
        <Tagline>Sabor que conquista, tradição que encanta</Tagline>
        <CTAButton href="#espetinhos">Ver Cardápio Completo</CTAButton>
      </HeroContent>
      <ScrollIndicator />
    </HeroContainer>
  );
};