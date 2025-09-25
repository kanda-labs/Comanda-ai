export enum ItemCategory {
  SKEWER = 'SKEWER',
  DRINK = 'DRINK',
  SNACK = 'SNACK',
  PROMOTIONAL = 'PROMOTIONAL'
}

export interface Item {
  id: number | null;
  name: string;
  value: number;
  category: ItemCategory;
  description?: string | null;
}

export interface CategoryGroup {
  category: ItemCategory;
  title: string;
  subtitle: string;
  items: Item[];
}

export const categoryTitles: Record<ItemCategory, { title: string; subtitle: string }> = {
  [ItemCategory.SKEWER]: {
    title: 'ESPETINHOS',
    subtitle: 'Grelhados na perfeição, temperados com paixão'
  },
  [ItemCategory.SNACK]: {
    title: 'PORÇÕES',
    subtitle: 'Para compartilhar momentos especiais'
  },
  [ItemCategory.DRINK]: {
    title: 'BEBIDAS',
    subtitle: 'Geladas na temperatura ideal'
  },
  [ItemCategory.PROMOTIONAL]: {
    title: 'PROMOÇÕES',
    subtitle: 'Aproveite nossas ofertas especiais'
  }
};