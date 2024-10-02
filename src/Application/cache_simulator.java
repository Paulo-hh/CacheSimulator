package Application;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.Random;
import java.util.Scanner;

public class cache_simulator {

	public static void main(String[] args) {
		
		// lendo a entrada no console
		Scanner sc = new Scanner(System.in);
		
		int nsets = sc.nextInt();
        int bsize = sc.nextInt();
        int assoc = sc.nextInt();
        char subst = sc.next().charAt(0);
        int flagOut = sc.nextInt();
        String arquivoEntrada = sc.next();
        Random aleatorio = new Random();
		
		sc.close();

		// Iniciando as variaveis
		
        int hit = 0;
        int miss = 0;
        int acessos = 0;
        int miss_conpulsorio = 0;
        int miss_conflito = 0;
        int miss_capacidade = 0;
        
        int[] cache_val = new int[nsets * assoc];
		int[] cache_tag = new int[nsets * assoc];
		
		int n_bits_offset = (int)(Math.log(bsize)/Math.log(2));
		int n_bits_indice = (int)(Math.log(nsets)/Math.log(2));
		int n_bits_tag = 32 - n_bits_offset - n_bits_indice;

		
		try (BufferedInputStream br = new BufferedInputStream(new FileInputStream(arquivoEntrada))) {
		
			int x, bytes;
			while((x = br.read()) != -1) {
				bytes = x;
				for(int i=0; i<3; i++) {
					bytes = bytes << 8;
					x = br.read();
					bytes = bytes | x;
				}
				int endereco = bytes;
				int tag = endereco >> (n_bits_offset + n_bits_indice);
				int indice = (endereco >> n_bits_offset) & ((int)Math.pow(2, n_bits_indice) - 1);
				acessos++;
				
				if(assoc == 1) {
					//Mapeamento Direto
					if (cache_val[indice] == 0){
						miss_conpulsorio++;
						miss++;
						cache_val[indice] = 1;
						cache_tag[indice] = tag;
					// estas duas últimas instruções representam o tratamento da falta.
					}
					else {
						if (cache_tag[indice] == tag) {
							hit++;
						}
						else{
							miss++;
							miss_conflito++;
							cache_val[indice] = 1;
							cache_tag[indice] = tag;
						} 
					}
				}
				else{
					if(nsets == 1) {
						//Mapeamento Totalmente Associativo
						boolean flag_hit = false;
						
						for(int i=0; i<assoc; i++) {
							if (cache_tag[i] == tag && cache_val[i] == 1) {
								flag_hit = true;
							}
						}
						
						if(flag_hit) {
							hit++;
						}
						else {
							miss++;
							boolean flag_miss_compulsorio = false;
							for(int i=0; i<assoc && flag_miss_compulsorio == true; i++) {
								if (cache_val[i] == 0) {
									miss_conpulsorio++;
									cache_val[i] = 1;
									cache_tag[i] = tag;
									flag_miss_compulsorio = true;
								}
							}
							if(flag_miss_compulsorio == false) {
								if(subst == 'R') {
									int value = aleatorio.nextInt(assoc);
									miss_capacidade++;
									cache_val[value] = 1;
									cache_tag[value] = tag;
								}
							}
						}
					}
			
					
					else {
						// Mapeamento conjunto-associativo
						boolean flag_hit = false;
						
						for(int i=(indice*assoc); i<(indice*assoc)+assoc; i++) {
							if (cache_tag[i] == tag && cache_val[i] == 1) {
								flag_hit = true;
							}
						}
						
						if(flag_hit) {
							hit++;
						}
						else {
							miss++;
							boolean flag_miss = false;
							for(int i=indice*assoc; i<(indice*assoc)+assoc && flag_miss == true; i++) {
								if (cache_val[i] == 0) {
									miss_conpulsorio++;
									cache_val[i] = 1;
									cache_tag[i] = tag;
									flag_miss = true;
								}
								else {
									if(cache_tag[i] != tag) {
										miss_conflito++;
										cache_val[i] = 1;
										cache_tag[i] = tag;
										flag_miss = true;
									}
								}
							}
							if(flag_miss == false) {
								if(subst == 'R') {
									int value = aleatorio.nextInt(assoc);
									miss_capacidade++;
									cache_val[(indice*assoc)+value] = 1;
									cache_tag[(indice*assoc)+value] = tag;
								}
							}
						}
						
					}
				}
				
			}
			if(flagOut == 1) {
				System.out.println();
				double taxa_hit = (double) hit / acessos;
				double taxa_miss = (double) miss / acessos;
				double taxa_miss_conpulsorio = (double) miss_conpulsorio / miss;
				double taxa_miss_capacidade = (double) miss_capacidade / miss;
				double taxa_miss_conflito = (double) miss_conflito / miss;
				System.out.printf("%d %.4f %.4f %.4f %.4f %.4f", acessos, taxa_hit, taxa_miss,
						taxa_miss_conpulsorio, taxa_miss_capacidade, taxa_miss_conflito);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
