export interface Chart {
  name: string;
  chartData: Data[];
}

export interface Data {
  name: string;
  series: {
    name: string;
    value: string;
  }[];
}