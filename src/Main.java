import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

//Ze wzgledu na to, że program jest krótki umieścilem wbrew podstawowym zasadom, w jednym pliku dwie klasy.
//Zrobiłem to w celu ulatwienia przegladania pliku na github

public class Main {
    
    public static void main(String[] args) {
        int IloscKolumn = 0;
        File file = new File("perceptron.data");
		
		//W Debug Configurations, Program arguments ustawiony na 0.5
		//Współczynnik uczenia, który określa, jak bardzo wagi są korygowane przy każdej aktualizacji, ja ustawilem na 0,5
        double WspolczynnikUczenia=Double.parseDouble(args[0]);
        //Wczytuje teta z zakresu [0-1)
		double Teta=Math.random();

        String[] NazwyKwiatow =new String[2];
        int licznik=0;

        Perceptron perceptron = null;
        try {
            Scanner scanner=new Scanner(file);
            if(scanner.hasNextLine()){
                String[] tab =scanner.nextLine().split(",");
				//Dodaje nazwe pierwszego kwiatu do tablicy NazwaKwiatow
                NazwyKwiatow[0]=tab[tab.length-1];
                licznik++;
                IloscKolumn = tab.length-1;
            }

			//Wczytuje losowe wagi z zakresu [0-1)
            double []Wagi = new double[IloscKolumn];
            for (int i=0;i<Wagi.length;i++){
                Wagi[i]=Math.random();
            }

            scanner=new Scanner(file);
			//Dodaje nazwe drugiego kwiatu do tablicy NazwaKwiatow
            while (scanner.hasNextLine()){
                String[] tab =scanner.nextLine().split(",");
                if(licznik==1&&!NazwyKwiatow[0].equals(tab[tab.length-1])){
                    NazwyKwiatow[licznik]=tab[tab.length-1];
                    licznik++;
                }
            }
			//Rozpoczynam proces uczenia perceptronu
            perceptron=new Perceptron(WspolczynnikUczenia,Teta,Wagi,NazwyKwiatow);

            int IloscIteracji=1;
            double Emax=0.003;
            double E=20;
			// W pętli sprawdzam czy zbiór jest separowalny. Być może zbiory 'nachodza na siebie',
			// w zwiazku z czym oddzielenie tych zbiorów może stać się niemożliwe
            while (IloscIteracji!=80&&E>Emax) {//E>Emax
                scanner=new Scanner(file);
                while (scanner.hasNextLine()) {
                    String[] tab = scanner.nextLine().split(",");

                    perceptron.DaneZPliku(tab);
                }

                E=perceptron.WartoscBleduIteracji();
                IloscIteracji++;
            }
            System.out.println(E);
            if(E>Emax){
                System.out.println("Dane są nieseparowalne lub Emax jest zdyt maly");
                System.exit(0);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
		
		//Ostatnim krokiem jest sprawdzenie czy perceptron nauczyl sie rozpoznawac gatunki
        File file1 = new File("perceptron.test.data");
        try{
            Scanner scanner=new Scanner(file1);
            while (scanner.hasNextLine()){
                String []WektorWyjsciowy=scanner.nextLine().split(",");
                perceptron.RozpoznajKwiat(WektorWyjsciowy);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("Poprawnie zaklasyfikowano "+Perceptron.PoprawnieZaklasyfikowane+" Ilosc klasyfikacji "+Perceptron.IloscKlasyfikacji);

    }
}

class Perceptron{
    double WspolczynnikUczenia;
    double Teta;
    double net=0;
    int y;
    int d;
    double[] Wagi;
    String []WektorWyjsciowy;
    String []NazwyKwiatow;
    double suma=0;
    static int D=0;
    static int PoprawnieZaklasyfikowane=0;
    static int IloscKlasyfikacji=0;

    Perceptron(double WspolczynnikUczenia, double Teta, double[] Wagi, String[] NazwyKwiatow){
        this.WspolczynnikUczenia=WspolczynnikUczenia;
        this.Teta=Teta;
        this.Wagi = Wagi;
        this.NazwyKwiatow=NazwyKwiatow;
    }

    public void DaneZPliku(String []WektorWyjsciowy){
        this.WektorWyjsciowy=WektorWyjsciowy;
		
		//Wartość net to iloczyn skalarny wektora wag oraz wektora wejść pomniejszony o wartość odchylenia
        for (int i=0;i<Wagi.length;i++){
            net=net+(Double.parseDouble(WektorWyjsciowy[i])*Wagi[i]);
        }
		
		//Pomniejszam o wartosc odchylenia
        net=net-Teta;

        this.WartoscWyjsciowa();
        this.RegulaDelta();
    }

    public void WartoscWyjsciowa(){
		//sprawdzam czy zbior danych jest pod prosta czy nad
       y = (net>=0?1:0);
       net=0;
        if(WektorWyjsciowy[WektorWyjsciowy.length-1].equals(NazwyKwiatow[0])){
            d=1;
        }else{
            d=0;
        }
    }

    public void RegulaDelta(){
		//Wagi i odchylenia modyfikujemy zgodnie z następującą regułą: w′=w+α(d−y)x θ′=θ−α(d−y)
        for(int i=0;i<Wagi.length;i++){
            Wagi[i]=Wagi[i]+(WspolczynnikUczenia*(d-y)*Double.parseDouble(WektorWyjsciowy[i]));
        }

        Teta=Teta-WspolczynnikUczenia*(d-y);
        suma=suma+Math.pow(d-y,2);

        D++;
    }

    public double WartoscBleduIteracji() {
        return suma/D;
    }

    public void RozpoznajKwiat(String []WektorWyjsciowy){
		//sprawdzanie po ktorej stronie plaszczyzny znajduja sie dane
        for (int i=0;i<Wagi.length;i++){
            net=net+(Double.parseDouble(WektorWyjsciowy[i])*Wagi[i]);
        }
        net=net-Teta;
        this.WartoscWyjsciowa();
        String DokladnoscKlasyfikacji;
        if(y==1){
            System.out.print(NazwyKwiatow[0]);
            DokladnoscKlasyfikacji=NazwyKwiatow[0];
        }else{
            System.out.print(NazwyKwiatow[1]);
            DokladnoscKlasyfikacji=NazwyKwiatow[1];
        }
		//sprawdzam poprawnosc klasyfikacji
        if(DokladnoscKlasyfikacji.equals(WektorWyjsciowy[WektorWyjsciowy.length-1])){
            PoprawnieZaklasyfikowane++;
            System.out.println(" zaklasyfikowano poprawnie");
        }
        IloscKlasyfikacji++;

    }
}
