import React from 'react';
import { Item } from '../../types/item';
import { MenuItem } from '../MenuItem/MenuItem';
import { MenuGrid } from '../CategorySection/CategorySection.styles';
import {
  ChoppSectionContainer,
  ChoppHeader,
  ChoppTitle,
  ChoppSubtitle
} from './ChoppSection.styles';

interface ChoppSectionProps {
  chopps: Item[];
}

export const ChoppSection: React.FC<ChoppSectionProps> = ({ chopps }) => {
  if (chopps.length === 0) return null;

  return (
    <ChoppSectionContainer id="chopps">
      <ChoppHeader>
        <ChoppTitle>CHOPPS UÇÁ</ChoppTitle>
        <ChoppSubtitle>Sempre na temperatura perfeita</ChoppSubtitle>
      </ChoppHeader>
      <MenuGrid>
        {chopps.map((chopp) => (
          <MenuItem key={chopp.id} item={chopp} isSpecial={true} />
        ))}
      </MenuGrid>
    </ChoppSectionContainer>
  );
};