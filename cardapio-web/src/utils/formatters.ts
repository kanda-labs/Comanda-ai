export const formatPrice = (valueInCents: number): string => {
  const valueInReais = valueInCents / 100;
  return new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL'
  }).format(valueInReais);
};

export const isChopp = (itemName: string): boolean => {
  const normalizedName = itemName.toLowerCase();
  return normalizedName.includes('chopp') ||
         normalizedName.includes('chop') ||
         normalizedName.includes('radler') ||
         normalizedName.includes('goré');
};