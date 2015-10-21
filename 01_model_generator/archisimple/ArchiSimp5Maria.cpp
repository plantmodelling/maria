/********************************************************************************/
/*     ARCHI SIMP5, Octobre 2012   (par rapport à la version 4, inclut la       */
/*     probabilité d'arrêt à chaque pas de temps)  ajout de 2 paramètres        */
/********************************************************************************/
#include <stdio.h>
#include <string>
#include <stdlib.h>
#include <math.h>
#include <unistd.h>
#include <time.h>
/*#include <windows.h>   // pour la version windows*/
#include <sys/time.h>  // pour la version linux
#include <iostream>
#include <iomanip>
#include <string>
#include <map>
#include <random>
#include <cmath>
/*#include <vector>
#include <chrono>
#include <Wincrypt.h>
#include <cstdint>*/


#define NBPASMAX 151 /* Nombre maximal de pas de temps */
#define NBHORMAX 60 /* Nombre maximal d'horizons de sol */
#define MAXLINE 150 /* Longueur maxi de la ligne dans fichiers texte */
#define NBCASEMAX 301  /* Nombre maximal de cases de sol en X, Y et Z */

const int deltaT=1;         /* Pas de temps, en jours */
const double epsilon=1.0e-10; /* Petite valeur, proche de 0 */
const double pi=3.141592653589793238462;  /* Valeur approchée de la constante Pi */
const double epaissHor=50.0;  /* Epaisseur horizons de sol (en mm) */
const float longSegNorm=5.0;  /* Longueur habituelle des segments formés (mm) */
const float longSegMin=2.0;  /* Longueur minimale des segments formés, quand faible croissance (mm) */
const int dureeSansCreation=3; /* Durée maximale sans création de noeud, quand faible croissance (jour) */
const double mailleMin=6.0;  /* Valeur de la maille minimale de sol (mm) */
const double d1=3.0;   /* Première valeur de distance (mm) */
const double d2=30.0;  /* Deuxième valeur de distance (plus grande) (mm) */

typedef float r2[2];  /* Tableau 2D */
typedef float r3[3];  /* Tableau 3D */

typedef struct SysRac *pTSysRac; /* Pour le système racinaire entier */
typedef struct Axe *pTAxe;   /* Pour chacun des axes */
typedef struct Pointe *pTPointe; /* Pour les pointes, parties apicales des racines */
typedef struct Seg *pTSeg; /* Pour les segments, qui sont des portions d'axes */

struct SysRac /* Ensemble d'axes */
  {
  long int nbAxeForm;  /* Nombre d'axes formés */
  long int nbAxeSup;   /* Nombre d'axes supprimés */
  long int nbSegForm;    /* Nombre de segments formés */
  long int nbSeg; /* Nombre de segments tels qu'ils sont comptés aux 3 dates */
  long int nbPrim;  /* Nombre de primaires émises */
  long int nbTard;  /* Nombre de racines tardives émises */
  float angDep;        /* Orientation */
  r3 origine;           /* Position de l'origine */
  pTAxe premAxe;       /* Premier axe du système (accès à la liste) */
  pTAxe dernAxe;       /* Dernier axe produit */
  float volMax[NBPASMAX];  /* Volume racinaire maximal pendant chaque pas de temps */
  float volDem[NBPASMAX];  /* Volume racinaire demandé pendant chaque pas de temps */
  float tSatis[NBPASMAX];  /* Taux de satisfaction de la demande à chaque pas de temps */
  float longueur; /* Longueur totale de racines */
  float profMax, profMoy; /* Profondeurs maximale et moyenne */
  float distMax, distMoy; /* Distances maximale et moyenne à l'axe du système */
  float diamMax; /* Diamètre maximal, du plus gros segment */
  float xbinf,ybinf,zbinf,xbsup,ybsup,zbsup; /* Bornes en x, y et z */
  float volProd,volPrim,volTot; /* Volumes racinaires : produit, primaire et total */
  float secPointe; /* Section totale des pointes matures et non séniles */
  float tSatisMoy; /* Taux de satisfaction moyen */
  float volSolD1, volSolD2; /* Volumes de sol à distance d1 et d2 */
  } ;

struct Pointe /* Méristème apical, ou pointe de chaque racine */
  {
  float distPrimInit;  /* Distance de l'apex au dernier primordium initié */
  float longueur;  /* Longueur non encore exprimée en allongement de l'axe */
  int dateDerniereCreation; /* Date à laquelle il y a eu création d'un noeud */
  r3 coord;           /* Coordonnées de la pointe */
  r3 dirCroiss;       /* Direction de croissance */
  r3 dirInit;         /* Direction initiale */
  float age;          /* Age du méristème */
  float diametre;     /* Diamètre de la pointe */
  unsigned char arretee;        /* Arrêtée ?, ou encore susceptible de s'allonger ... */
  unsigned char senile;         /* Sénile ?, ou encore actif ... */
  unsigned char mature;         /* Mature ?, ou encore au stade primordium ... */
  } ;

struct Axe /* Ensemble constitué d'un méristème et liste de noeuds */
  {
  long int num;      /* Numéro de l'axe */
  long int parentNum; /*Identifier of the parent*/
  long int nbSeg;       /* Nombre de noeuds */
  pTPointe pointe; /* Méristème apical */
  pTAxe suivant;     /* Suivant de la liste */
  pTAxe precedent;   /* Précédent de la liste */
  pTAxe pere;        /* Axe père, sur lequel celui-ci est branché */
  pTSeg premSeg; /* Premier segment de l'axe, sa base */
  pTSeg dernSeg; /* Dernier segment de l'axe, apical */
  } ;

struct Seg
  {
  long int num;      /* Numéro d'ordre de création */
  int jourForm;      /* Date de formation (en jours) */
  unsigned char complet; /* Complet (1), c'est-à-dire avec ses deux points, ou non (0) */
  float diametre;    /* Diametre */
  r3 posO;            /* Position dans l'espace de son origine */
  r3 posE;            /* Position dans l'espace de son extrêmité */
  pTSeg suiv;      /* Suivant dans le prolongement (NULL sinon) */
  pTSeg prec;     /* Précédent, sur le même axe quand non base, sur axe père sinon */
  pTAxe axe;        /* Axe auquel appartient le segment */
  unsigned char necrose;       /* Necrose ? 0 : non; 1 : oui */
  } ;

struct Horizon  /* Horizon de sol */
  {
  float croiss;  /* Coefficient de croissance, compris entre 0 et 1 */
  float ramif;   /* Coefficient multiplicateur de distance inter-ramif  */
  float iCMeca;  /* Intensité de la contrainte mécanique */
  int oCMeca;    /* Orientation de la contrainte mécanique (O iso, ou 1 vert) */
  } ;

typedef Horizon TSol[NBHORMAX];  /* Sol pour la croissance, tableau d'horizons */

/* Fichiers */
FILE *FSeg;  /* Fichier contenant la structure sous forme de segments */
FILE *FPar;    /* Paramètres */
FILE *FSol;    /* Informations sur le sol, par horizons */
FILE *FVol;    /* Informations sur le volume racinaire possible, à chaque pas de temps */
FILE *FDem;     /*MODIFBEN : rajout du fichier FDem pour le monitoring de la demande en carbone*/
// FILE *FAudit;  /* Audit sur le déroulement de la simulation */
// FILE *FSynth;  /* Fichier contenant des variables de synthèse */
// FILE *FVox;    /* Informations sur les voxels colonisés */

/* Paramètres, lus dans fichier paramètres */
int P_duree=1000; /* Durée de la simulation, en jours */



// Caractérisation de l'émission des primaires
int P_simultEmiss=1; /*MODIFBEN : switch pour la modification permettant l'émission simultanée de 3 séminales 3 jours après la principale*/
float P_vitEmissionPrim=0.5; /* Vitesse d'émission des primaires (en jour-1) */ /*MODIFBEN : ajout du 1 pour dédoublement*/
int P_nbMaxPrim=1; /* Nombre maximal de racines primaires */

float P_angInitMoyVertPrim=1; /* Angle d'insertion moyen par rapport à la verticale pour les primaires */
float P_angInitETVertPrim=0.5;  /* écart-type de l'angle d'insertion des primaires */
float P_slopePrimAngle = 0.1; /*Slope of the function between the insertion angle of the primary and the age of the plant*/


// Caractérisation de l'émission des tardives
float P_ageEmissionTard=15.0; /* âge de commencement de l'émission des racines tardives */
float P_vitEmissionTard=2.0; /* Vitesse d'émission des tardives (en jour-1) */
float P_dBaseMaxTard=80.0; /* Distance à la base maximale pour les tardives (mm) */
float P_propDiamTard=0.70; /* Proportion du diamètre des tardives (par rapport aux primaires) */
int P_nbSeminales=4; /*MODIFBEN : nombre de séminales*/
int P_nbMaxTard=40; /* Nombre maximal de racines tardives */

float P_angInitMoyVertTard=1.57; /* Angle d'insertion moyen par rapport à la verticale pour les tardives */
float P_angInitETVertTard=0.02;  /* écart-type de l'angle d'insertion des tardives */

// Probabilité journalière d'arrêt de la croissance
float P_probaMaxArret=0.2;   /* Probabilité journalière d'arrêt maximale, pour un diamètre nul */
float P_probaEffetDiam=2.0;  /* Effet de décroissance de la probabilité, en lien avec diamètre apical (mm-1) */

// Croissance radiale
float P_coeffCroissRad=0.8; // coefficient de croissance radiale

// Allongement (croissance axiale)
float P_diamMin=0.10;  /* Diamètre minimal en deça duquel il n'y a pas de croissance (mm) */
float P_diamMax=1.2;   /* Diamètre maximal donné aux racines primaires (mm) */
float P_penteVitDiam=12.0; /* pente de la relation entre vitesse de croissance et diamètre (mm.mm.jour-1) */
int P_tendanceDirTropisme=1;  /* Type de tropisme (0: plagio; -1: geo-; +1: geo+; 2: exo */
float P_intensiteTropisme=0.2; /* Coefficient multiplié par le diamètre pour chaque racine */

// Ramification
float P_ageMaturitePointe=1.5;  /* âge de maturité des méristèmes (jours) */
float P_distRamif=4.0; /* distance inter-ramification (mm) */
float P_propDiamRamif=0.5; /* proportion de diamètre des filles par rapport à leur mère */
float P_coeffVarDiamRamif=0.30; /* coefficient de variation du diamètre des ramifs */
float P_angLat=2.6; /* angle d'insertion des racines latérales */
int P_arabido=0; /* is the plant an arabidopsis? */
int P_tertiary=0; /* Print the tertiary roots */
int P_counter=0;

// Mortalité
float P_TMD=0.2; /* Tissue mass density, ou masse volumique */
float P_penteDureeVieDiamTMD=2000.0; /* pente de la relation durée de vie versus diamètre et TMD */

// MODIFBEN : Mortalité programmée
int P_sacrifice=0; /* Swith on/off pour le sacrifice*/
int P_condemnedRoot=50; /* ID de la racine condamnée */
int P_sacrificeTime=10; /* Temps auquel la racine est sacrifiée */

/* Variables globales diverses */

long temps = 0;  /* Le temps, en jours */
r3 orig;      /* Position d'origine du système racinaire */
char P_outputName [100] = "output.txt";  /* Name of the output file*/
char P_outputName2 [150] = "output2.txt"; //MODIFBEN : ajout d'un deuxième outputname pour inclure le pas de temps
int P_type = 1; /*Switch: 1 = 3D ; 2 = 2D ; 3 = Shovelomics*/
float P_IC_meca = 0.02; /*Soil constrain */
int P_shovel = 20; /* Depth of the shovel, for the shovelomics simulations*/ 
float P_maxLatAge = 20; /*Maximum age for a lateral root*/

int dpi = 30;
float scale = (dpi ) / 2.54;

unsigned char vox[NBCASEMAX+1][NBCASEMAX+1][NBCASEMAX+1];  /* tableau sol-voxel dont les cases vont contenir des entiers 1, 2 ou 3 */

float maille=mailleMin; /* Valeur initialisée de la maille de sol */
double volElemSol;  /* Volume élémentaire de sol associé à la maille (mm3) */

pTSysRac sR;  /* Le système racinaire */
TSol sol;     /* Le sol */
/****************************************************************************/
/*MODIFBEN : rajout d'un RNG basé sur le générateur Windows*/
/*uint64_t getRandomSeed()
{
    uint64_t ret;
    HCRYPTPROV hp = 0;
    CryptAcquireContext(&hp, 0, 0, PROV_RSA_FULL, CRYPT_VERIFYCONTEXT);
    CryptGenRandom(hp, sizeof(ret), reinterpret_cast<uint8_t*>(&ret));
    CryptReleaseContext(hp, 0);
    return ret;
}*/
/****************************************************************************/
double dRandUnif(void)
/* Cette fonction tire un aléatoire uniforme réel entre 0 et 1 */
{
  double tirage;
    // Seed with a real random value, if available
  std::random_device rd;

  // Choose a random mean between 1 and 6
  std::default_random_engine e1(rd());
  std::uniform_real_distribution<double> uniform_dist(0.9,1);
  tirage = uniform_dist(e1);
//  tirage=(double) rand()/(double) RAND_MAX;
  if (tirage<epsilon) { tirage=epsilon; }
  return(tirage);
}

/****************************************************************************/
double dRandUnif2(void)
/* Cette fonction tire un aléatoire uniforme réel entre 0 et 1 */
{
  double tirage;
    // Seed with a real random value, if available
  std::random_device rd;

  // Choose a random mean between 1 and 6
  std::default_random_engine e1(rd());
  std::uniform_real_distribution<double> uniform_dist(0,1);
  tirage = uniform_dist(e1);
//  tirage=(double) rand()/(double) RAND_MAX;
  if (tirage<epsilon) { tirage=epsilon; }
  return(tirage);
}

/****************************************************************************/
/****************************************************************************/
void norme(r3 u, r3 un)
/* Cette fonction norme le vecteur u de l'espace de dimension 3.
  Le vecteur norme de retour est appele un. */
{
double norU;
  norU=sqrt((u[0]*u[0])+(u[1]*u[1])+(u[2]*u[2]));
  if (norU<epsilon)
  {
  printf("ATTENTION, vecteur nul ! Sa norme vaut : %f \n",norU);
  exit(1);
  }
  else
  {
   un[0]=u[0]/norU;
   un[1]=u[1]/norU;
   un[2]=u[2]/norU;
  }
}  /* Fonction Norme */
/****************************************************************************/
/****************************************************************************/
double prodScal(r3 u,r3 v)
/* Cette fonction retourne le produit scalaire de 2 vecteurs u et v de
  l'espace a 3 dimensions. */
{
double prodScal;
  prodScal=(u[0]*v[0])+(u[1]*v[1])+(u[2]*v[2]);
  return(prodScal);
}  /* Fonction prodScal */
/****************************************************************************/
/****************************************************************************/
void prodVect(r3 u, r3 v, r3 u_vect_v)
/* Cette fonction calcule le produit vectoriel de deux vecteurs u et v
  de l'espace de dimension 3. Le vecteur resultant est u_vect_v. */
{
  u_vect_v[0]=(u[1]*v[2])-(v[1]*u[2]);
  u_vect_v[1]=(u[2]*v[0])-(v[2]*u[0]);
  u_vect_v[2]=(u[0]*v[1])-(v[0]*u[1]);
}   /* Fonction prodVect */
/****************************************************************************/
/****************************************************************************/
void rotVect(double omega, r3 u, r3 x, r3 rot_x)

/* Cette fonction calcule le vecteur rot_x dans l'espace de dimension 3,
  issu de la rotation du vecteur x autour d'un axe dont u est un vecteur
  unitaire. La rotation se fait d'un angle omega radians. Elle appelle
  PRODSCAL, PRODVECT. */
{
double uscalx;   /* produit scalaire u.x  */
r3    uvectx;   /* produit vectoriel u^x */

  uscalx=prodScal(u,x);
  prodVect(u,x,uvectx);

  rot_x[0]=((1-cos(omega))*uscalx*u[0])
      +(cos(omega)*x[0])+(sin(omega)*uvectx[0]);
  rot_x[1]=((1-cos(omega))*uscalx*u[1])
      +(cos(omega)*x[1])+(sin(omega)*uvectx[1]);
  rot_x[2]=((1-cos(omega))*uscalx*u[2])
      +(cos(omega)*x[2])+(sin(omega)*uvectx[2]);

}  /* Fonction rotVect */
/****************************************************************************/
/****************************************************************************/
void rotZ(r3 u, r3 v, double teta)
/* Cette fonction fait tourner "u" d'un angle "teta" autour de l'axe (Oz);
  le vecteur calcule est "v" */
{
  v[0]=(u[0]*cos(teta))-(u[1]*sin(teta));
  v[1]=(u[0]*sin(teta))+(u[1]*cos(teta));
  v[2]=u[2];
}
/****************************************************************************/
/****************************************************************************/
int iRandUnif(int imax)

/* Cette fonction tire un aléatoire uniforme entier entre 0 et imax */
{
  int tirage;

  tirage=imax+1;
  while (tirage>imax) tirage=rand();
  return tirage;
}
/****************************************************************************/
/****************************************************************************/
void ouvreFichiers(void)
/* Cette fonction ouvre les fichiers, en lecture*/
{

//  FAudit=fopen("audit.txt","w");
  FPar=fopen("param.txt","rt");
  FSol=fopen("sol.txt","rt");
  FVol=fopen("volrac.txt","rt");
  /*MODIFBEN : ouverture d'un fichier de monitoring de la demande en volume*/
  FDem=fopen("demand.csv", "a");
//  FSynth=fopen("synth.txt","w");
//  FVox=fopen("vox.txt","w");
} /* Fonction ouvreFichiers */

/****************************************************************************/
/****************************************************************************/
void ouvreFichiersOutput(void)
/* Cette fonction ouvre les fichiers, en écriture */
{
  sprintf(P_outputName2, "%s-%ld.rsml", P_outputName, temps); //MODIFBEN : ajout d'un outputname incluant le pas de temps
  //printf("%s", P_outputName2); // MODIFBEN : ATTENTION, nécessite que le batch R soit adapté en conséquence.
  FSeg=fopen(P_outputName2,"w");
//  FSynth=fopen("synth.txt","w");
//  FVox=fopen("vox.txt","w");
} /* Fonction ouvreFichiers */

/****************************************************************************/
/****************************************************************************/
void litSol(void)
/* Fonction de lecture des caractéristiques du sol, une ligne par horizon */
{
int hor;              /* Compteur des horizons */
char bid[MAXLINE];    /* Chaîne qui accueille les caractères supplémentaires */

fgets(bid,MAXLINE-1,FSol);          /* Ligne entête */
for (hor=0; hor<NBHORMAX; hor++)
{
  fscanf(FSol,"%f %f %f %i",&sol[hor].croiss,&sol[hor].ramif,&sol[hor].iCMeca,&sol[hor].oCMeca);
//  fscanf(FSol,"%f",&sol[hor].croiss); // Favorable à la croissance
//  fscanf(FSol,"%f",&sol[hor].ramif);  // Favorable à la ramification
//  fscanf(FSol,"%f",&sol[hor].iCMeca); // Intensité de la contrainte
//  fscanf(FSol,"%d",&sol[hor].oCMeca); // Orientation 0: iso, 1: verticale
  fgets(bid,MAXLINE-1,FSol);
}

} /* Fonction litSol */
/****************************************************************************/
/****************************************************************************/
void litVolumeMaxSR(pTSysRac sR)
/* Fonction de lecture des volumes maximaux à chaque pas de temps */
{
int pas;              /* Compteur des pas de temps */

char bid[MAXLINE];    /* Chaîne qui accueille les caractères supplémentaires */

fgets(bid,MAXLINE-1,FVol);          /* Ligne entête */
for (pas=0; pas<NBPASMAX; pas++)
{
  fscanf(FVol,"%f",&(sR->volMax[pas]));
  fgets(bid,MAXLINE-1,FVol);
}

} /* Fonction litVolume */
/****************************************************************************/
/****************************************************************************/
double croissSol(TSol sol, double profondeur)
/* Renvoie le coefficient de croissance du sol à la Profondeur donnée */
{
int hor;

  hor=(int) floor(profondeur/epaissHor);
  if (hor>=NBHORMAX) hor=NBHORMAX-1;
  if (hor<0) hor=0;

  return(sol[hor].croiss);
} /* Fonction croissSol */
/****************************************************************************/
/****************************************************************************/
double ramifSol(TSol sol, double profondeur)
/* Renvoie le coefficient de ramification du sol à la profondeur donnée */
{
int hor;

  hor=(int) floor(profondeur/epaissHor);
  if (hor>=NBHORMAX) hor=NBHORMAX-1;
  if (hor<0) hor=0;

  return(sol[hor].ramif);
} /* Fonction ramifSol */
/****************************************************************************/
/****************************************************************************/
double iCMecaSol(TSol sol, double profondeur)
/* Renvoie l'intensité de la contraine méca du sol à la Profondeur donnée */
{
int hor;

  hor=(int) floor(profondeur/epaissHor);
  if (hor>=NBHORMAX) hor=NBHORMAX-1;
  if (hor<0) hor=0;

  return(sol[hor].iCMeca);
} /* Fonction iCMecaSol */
/****************************************************************************/
/****************************************************************************/
int oCMecaSol(TSol sol, double profondeur)
/* Renvoie l'indice de la direction de contrainte : 0 pour iso, 1 pour verti */
{
int hor;

  hor=(int) floor(profondeur/epaissHor);
  if (hor>=NBHORMAX) hor=NBHORMAX-1;
  if (hor<0) hor=0;

  return(sol[hor].oCMeca);
} /* Fonction oCMecaSol */
/****************************************************************************/
/****************************************************************************/
double tireGaussien(float moy, float et)
{  /* Réalise un tirage gaussien dans une distribution de moyenne moy et écart-type et */
  double tireGaussien,tire1,tire2;

  tire1=dRandUnif();
  tire2=dRandUnif();
  tireGaussien=moy+(et*sqrt(-log(tire1))*cos(pi*tire2)*1.414);
  return(tireGaussien);
} /* Fonction tireGaussien */
/****************************************************************************/
/****************************************************************************/
double tireAngRad(void)
{   /* Tire l'angle radial dans l'intervalle 0 - 2*Pi */
  if(P_arabido == 1){
    if(dRandUnif() < 0.5) return pi;
    else return 0;
  }else{
    return (2.0*pi*dRandUnif2());
  }
} /* Fonction TireAngRad */
/****************************************************************************/
/****************************************************************************/
void increNbSegSR(pTSysRac sR)
/* Incrémente le nombre de noeuds qui a été formé dans ce système sR */
{
  sR->nbSegForm++;
} /* Fonction increNbSegSR */
/****************************************************************************/
/****************************************************************************/
pTSeg creeSeg(void)
/* Cette fonction retourne une nouvelle variable de type pTSeg,
  c'est-à-dire un pointeur sur le type Seg */
{
pTSeg seg;
  seg=(pTSeg) malloc(sizeof(Seg));
  if (seg==NULL)
  { printf("Problème mémoire allocation dans creeSeg \n"); exit(1); }

return seg;
} /* Fonction creeSeg */
/****************************************************************************/
/****************************************************************************/
pTSeg initialiseSeg(long int num, r3 posOrig, r3 posExtrem, double diam, pTAxe axeSeg, unsigned char comp, pTSeg precedent)
/* Cette fonction retourne une nouvelle variable de type pTSeg,
  dont une partie des valeurs est initialisée */
{
pTSeg seg;

  seg=creeSeg();

  seg->num=num;
  seg->jourForm=temps;
  seg->necrose=0;
  seg->complet=comp;

  seg->diametre=diam;
  seg->axe=axeSeg;

  seg->posO[0]=posOrig[0];
  seg->posO[1]=posOrig[1];
  seg->posO[2]=posOrig[2];

  seg->posE[0]=posExtrem[0];
  seg->posE[1]=posExtrem[1];
  seg->posE[2]=posExtrem[2];

  seg->suiv=NULL;  // pour l'instant
  seg->prec=precedent;

return seg;
} /* Fonction initialiseSeg */
/****************************************************************************/
/****************************************************************************/
void detruitSeg(pTSeg segADetruire)
/* Supprime un noeud en mémoire */
{
  free(segADetruire);
} /* Fonction detruitSeg */
/****************************************************************************/
/****************************************************************************/
pTPointe creePointe(void)
/* Cette fonction retourne une nouvelle variable de type pTPointe,
  c'est-à-dire un pointeur sur le type Pointe */
{
pTPointe pointe;
  pointe=(pTPointe) malloc(sizeof(Pointe));
  if (pointe==NULL)
  { printf("Problème mémoire allocation dans creePointe \n"); exit(1); }

return pointe;
} /* Fonction creePointe */
/****************************************************************************/
/****************************************************************************/
pTPointe initialisePointe(float diam, r3 position, r3 direction)
/* Cette fonction retourne une nouvelle variable de type pTPointe,
  dont les valeurs sont en partie initialisées */
{
pTPointe pointe;

  pointe=creePointe();

  pointe->distPrimInit=0.0;
  pointe->longueur=0.0;
  pointe->age=0.0;
  pointe->diametre=diam;
  pointe->arretee=0;
  pointe->senile=0;
  pointe->mature=0;

  pointe->coord[0]=position[0];
  pointe->coord[1]=position[1];
  pointe->coord[2]=position[2];

  pointe->dirCroiss[0]=direction[0];
  pointe->dirCroiss[1]=direction[1];
  pointe->dirCroiss[2]=direction[2];

  pointe->dirInit[0]=direction[0];
  pointe->dirInit[1]=direction[1];
  pointe->dirInit[2]=direction[2];


return pointe;
} /* Fonction initialisePointe */
/****************************************************************************/
/****************************************************************************/
void deflecMecaPointe(pTPointe pointe, r3 dirApresMeca, double elong)
{
const double teta=15.0; /* Angle autour de G, en degres */

r3 vTire,vTireN,dirInt;
double profondeur, cont;

  profondeur=pointe->coord[2];
  cont=P_IC_meca;

  if (P_type == 2)    /* Contrainte 2D - GL */
    {
      do
      {
      vTire[0]=(2.0*dRandUnif2()-1.0)*sin(pi*teta/180.0);
      vTire[1]=0;
      do { vTire[2]=dRandUnif2(); } while (vTire[2]>cos(pi*teta/180.0));
      norme(vTire,vTireN);
      }  while (vTireN[2]>cos(pi*teta/180.0));
      dirInt[0]=pointe->dirCroiss[0]+(elong*vTireN[0] * cont);
      dirInt[1]=0;
      dirInt[2]=pointe->dirCroiss[2]+(elong*vTireN[2] * cont);
  }
  else  /*No constrain - 3D */
  {
      if (oCMecaSol(sol,profondeur)==1)  /* Contrainte anisotrope verticale */
      {
      /* Tirage vecteur dans l'angle Teta autour de G */
      do
      {
      vTire[0]=(2.0*dRandUnif2()-1.0)*sin(pi*teta/180.0);
      vTire[1]=(2.0*dRandUnif2()-1.0)*sin(pi*teta/180.0);
      do { vTire[2]=dRandUnif2(); } while (vTire[2]>cos(pi*teta/180.0));
      norme(vTire,vTireN);
      }  while (vTireN[2]>cos(pi*teta/180.0));
      dirInt[0]=pointe->dirCroiss[0]+(elong*vTireN[0]*cont);
      dirInt[1]=pointe->dirCroiss[1]+(elong*vTireN[1]*cont);
      dirInt[2]=pointe->dirCroiss[2]+(elong*vTireN[2]*cont);
      }
      else    /* Contrainte isotrope [oCMecaSol(Profondeur)==0] */
        {
        vTire[0]=2.0*dRandUnif2()-1.0;
        vTire[1]=2.0*dRandUnif2()-1.0;
        vTire[2]=2.0*dRandUnif2()-1.0;
        norme(vTire,vTireN);
      if (prodScal(vTireN,pointe->dirCroiss)<0.0)
        {
        vTireN[0]=-vTireN[0];
        vTireN[1]=-vTireN[1];
        vTireN[2]=-vTireN[2];
      }
      dirInt[0]=pointe->dirCroiss[0]+(elong*vTireN[0]*cont);
      dirInt[1]=pointe->dirCroiss[1]+(elong*vTireN[1]*cont);
      dirInt[2]=pointe->dirCroiss[2]+(elong*vTireN[2]*cont);
      }
  }
  norme(dirInt,dirApresMeca);

} /* Fonction deflecMecaPointe */
/****************************************************************************/
/****************************************************************************/
void deflecGeoPointe(pTPointe pointe, r3 dirApresMeca, r3 dirApresGeo, double elong)
/* Version avec plagiotropisme */
{
r3 dirInt,vGeoInt,vGeo;

  switch (P_tendanceDirTropisme) {
    case -1 : vGeo[0]=0.0;                  /* Gravitropisme négatif */
              vGeo[1]=0.0;
              vGeo[2]=-1.0;
              break;
    case 0 : vGeoInt[0]=pointe->dirInit[0]; /* Plagiotropisme */
             vGeoInt[1]=pointe->dirInit[1];
             vGeoInt[2]=0.0;
             norme(vGeoInt,vGeo);
             break;
    case 1 : vGeo[0]=0.0;                  /* Gravitropisme positif */
             vGeo[1]=0.0;
             vGeo[2]=1.0;
              break;
    case 2 : vGeoInt[0]=pointe->dirInit[0]; /* Exotropisme */
             vGeoInt[1]=pointe->dirInit[1];
             vGeoInt[2]=pointe->dirInit[2];
             norme(vGeoInt,vGeo);
             break;
    default : vGeo[0]=0.0;                 /* Gravitropisme positif */
              vGeo[1]=0.0;
              vGeo[2]=1.0;
              break;
  }

  dirInt[0]=dirApresMeca[0]+(vGeo[0]*P_distRamif*elong*pointe->diametre);
  dirInt[1]=dirApresMeca[1]+(vGeo[1]*P_intensiteTropisme*elong*pointe->diametre);
  dirInt[2]=dirApresMeca[2]+(vGeo[2]*P_intensiteTropisme*elong*pointe->diametre);

  norme(dirInt,dirApresGeo);
} /* Fonction deflecGeoPointe */
/****************************************************************************/
/****************************************************************************/
void deflecSurfPointe(pTPointe Pointe, r3 dirApresGeo, r3 dirApresSurf)
{
const double profLim=50.0*dRandUnif();
r3 dirInt;
  dirInt[0]=dirApresGeo[0];
  dirInt[1]=dirApresGeo[1];
  dirInt[2]=dirApresGeo[2];

  if ((dirInt[2]<0.0) && ((Pointe->coord[2])<profLim)) dirInt[2]=dirInt[2]/10.0;
  norme(dirInt,dirApresSurf);
} /* Fonction deflecSurfPointe */
/****************************************************************************/
/****************************************************************************/
void reorientePointe(pTPointe pointe, double elong)
{
r3 dirInt1, dirInt2, nouvDir;

  deflecMecaPointe(pointe,dirInt1,elong);
  deflecGeoPointe(pointe,dirInt1,dirInt2,elong);
  deflecSurfPointe(pointe,dirInt2,nouvDir);

  pointe->dirCroiss[0]=nouvDir[0];
  pointe->dirCroiss[1]=nouvDir[1];
  pointe->dirCroiss[2]=nouvDir[2];


} /* Fonction reorientePointe */
/****************************************************************************/
/****************************************************************************/
double calcElongationPointe(pTPointe pointe, TSol sol)
/* Calcul de l'élongation potentielle affectée par le sol */
{
  if (!pointe->arretee && !pointe->senile && pointe->mature && (pointe->diametre>P_diamMin))
    return (pointe->diametre - P_diamMin)*deltaT*P_penteVitDiam*croissSol(sol,pointe->coord[2]);
  else return 0.0;

} /* Fonction calcElongationPointe */
/****************************************************************************/
/****************************************************************************/
void vieillitPointe(pTPointe pointe)
{ /* Incrémente l'âge du méristème selon le pas de temps */
  pointe->age=pointe->age+deltaT;
} /* Fonction vieillitPointe */
/****************************************************************************/
/****************************************************************************/
void matureSenescePointe(pTPointe pointe)
{ /* Assure l'évolution du primordium en pointe si son âge est atteint
     et le rend sénile si son âge est atteint */

  if ((pointe->mature)&&(!pointe->senile)&&(temps>10))
  {
	  double tirage=dRandUnif();
	  double probaArret=P_probaMaxArret*(1-exp(-P_probaEffetDiam*pointe->age/pointe->diametre));
	  if (tirage < probaArret) pointe->arretee=1; /* La pointe s'arrête */
  }
  if ((pointe->mature)&&(pointe->age>(P_penteDureeVieDiamTMD*pointe->diametre*P_TMD)))
  {
    pointe->senile=1;  /* La pointe devient sénile */
  }
  if ((!pointe->mature)&&(pointe->age>P_ageMaturitePointe))
  {
    pointe->mature=1;  /* Le primordium devient méristème vrai */
    pointe->age=0.0;   /* Son âge est réinitialisé à 0 en tant que pointe mature */
  }
} /* Fonction matureSenescePointe */
/****************************************************************************/
/****************************************************************************/
void developpePointe(pTPointe pointe)
{ /* Assure l'évolution de la pointe */

  /*MODIFBEN : adaptation du diamètre de la pointe en fonction du taux de satis réel, 
  puisqu'il est adapté lors de l'étape de calctauxsatis*/

  double volDemande=sR->volDem[temps];
  double volDisponible=sR->volMax[temps] * 1;
  float tauxReel=volDisponible/volDemande;

  /*
  if (tauxReel<0.7)
  {
    pointe->diametre=pointe->diametre-((pointe->diametre-pointe->diametre*0.85)/deltaT); //valeurs tirées de GRAAL, limite inférieure changée de 0.6 à 0.7, étant déjà une limite inférieure de ce modèle-ci
  }
  if (tauxReel>0.9)
  {
    pointe->diametre=pointe->diametre+((pointe->diametre*1.15-pointe->diametre)/deltaT); //Idem.
  }
  */

  vieillitPointe(pointe);
  matureSenescePointe(pointe);
} /* Fonction developpePointe */
/****************************************************************************/
/****************************************************************************/
void deplacePointe(pTPointe pointe, double elong)
{ /* Assure le déplacement du méristème suite à croissance axiale */

  /* Sa position est modifiée */
  pointe->coord[0]=pointe->coord[0]+(elong*pointe->dirCroiss[0]);
  pointe->coord[1]=pointe->coord[1]+(elong*pointe->dirCroiss[1]);
  pointe->coord[2]=pointe->coord[2]+(elong*pointe->dirCroiss[2]);

  /* Son attribut distPrimInit est modifié */
  pointe->distPrimInit+=elong;

} /* Fonction deplacePointe */
/****************************************************************************/
/****************************************************************************/
double distInterRamifPointe(pTPointe pointe, TSol sol)
{ /* Renvoie la valeur locale de la distance inter-ramification de la pointe */

  return (P_distRamif*ramifSol(sol,pointe->coord[2]));

} /* Fonction distInterRamifPointe */
/****************************************************************************/
/****************************************************************************/
void detruitPointe(pTPointe pointeADetruire)
/* Supprime une pointe */
{
  free(pointeADetruire);
} /* Fonction detruitPointe */
/****************************************************************************/
/****************************************************************************/
pTAxe creeAxe(void)
/* Cette fonction retourne une nouvelle variable de type pTAxe,
  c'est-à-dire un pointeur sur le type Axe */
{
pTAxe axe;
  axe=(pTAxe) malloc(sizeof(Axe));
  if (axe==NULL)
  { printf("Problème mémoire allocation dans creeAxe \n"); exit(1); }

return axe;
} /* Fonction creeAxe */
/****************************************************************************/
/****************************************************************************/
pTAxe initialiseAxe(long int numAxe, float diamPointe, r3 origine, r3 dirInit, pTAxe axePere, pTSeg segPorteur)
/* Cette fonction retourne une nouvelle variable de type pTAxe,
  c'est-à-dire un pointeur sur le type TAxe */
{
  pTAxe nouvAxe;
  pTSeg premierSeg;

  nouvAxe=creeAxe();
  premierSeg=initialiseSeg(sR->nbSegForm+1,origine,origine,diamPointe,nouvAxe,0,segPorteur);
  nouvAxe->pointe=initialisePointe(diamPointe,origine,dirInit);
  nouvAxe->premSeg=premierSeg;
  nouvAxe->dernSeg=premierSeg;
  nouvAxe->nbSeg=1;
  nouvAxe->num=numAxe;
  nouvAxe->pere=axePere;

  nouvAxe->suivant=NULL;
  nouvAxe->precedent=NULL;

  return nouvAxe;
} /* Fonction initialiseAxe */
/****************************************************************************/
/****************************************************************************/
void ajouteSegProlongeAxe(pTAxe axe, pTSeg segAAjouter)
/* Cette fonction ajoute un segment de prolongement en position apicale
à l'axe concerné, et incrémente son compteur de segments */
{
pTSeg ancienSegTerm;

  ancienSegTerm=axe->dernSeg;

  // Si ce dernier segment est complet, il faut prolonger la liste
  if (ancienSegTerm->complet) {
    ancienSegTerm->suiv=segAAjouter;
    segAAjouter->prec=ancienSegTerm;
    axe->dernSeg=segAAjouter;
    axe->nbSeg++;
  }
  // Sinon, il faut juste compléter le dernier segment
  else {
    // rien à faire
    // les mises à jour sont faites dans developpeAxeSR
    // on ne doit pas passer ici
  }

} /* Fonction ajouteSegProlongeAxe */
/****************************************************************************/
/****************************************************************************/
void ajouteAxeSR(pTSysRac sR, pTAxe axeAAjouter)
/* Cette fonction insère un axe dans la chaîne des axes du système racinaire,
elle incrémente en même temps le compteur d'axes et de segments */
{
  if (sR->premAxe==NULL)  /* Le système racinaire est vide */
  {
    axeAAjouter->suivant=NULL;
    axeAAjouter->precedent=NULL;
    sR->premAxe=axeAAjouter;
    sR->dernAxe=axeAAjouter;
  }
  else /* Le système contient déjà des axes, assure le chaînage double des axes */
  {
    axeAAjouter->suivant=NULL;
    axeAAjouter->precedent=sR->dernAxe;
    sR->dernAxe->suivant=axeAAjouter;
    sR->dernAxe=axeAAjouter;
  }
  sR->nbAxeForm++;
  sR->nbSegForm++;   // à chaque axe, un segment

} /* Fonction ajouteAxeSR */
/****************************************************************************/
/****************************************************************************/
int axeRamifiable(pTAxe axe)
{   /* Renvoie 1 ou 0 suivant que l'axe est ramifiable ou non */

return(axe->pointe->distPrimInit > P_distRamif);

} /* Fonction axeRamifiable */
/****************************************************************************/
/****************************************************************************/
float tireDiamPointeFille(pTAxe axePere)
{
/* Tire le diamètre d'un méristème de ramification suivant celui du père
       pour la ramification séquentielle */

  float moy=axePere->pointe->diametre*P_propDiamRamif + (P_diamMin*(1.0-P_propDiamRamif));
  float et=moy*P_coeffVarDiamRamif;
  float diamPFille=10.0;  // initialisation à une forte valeur
  while (diamPFille>(1.1*axePere->pointe->diametre)) diamPFille=tireGaussien(moy,et);

  return diamPFille;

} /* Fonction tireDiamPointeFille */
/****************************************************************************/
/****************************************************************************/
void origineTard(pTSeg segPere, r3 origineFils)
{   /* Calcule la position du point d'origine d'une tardive sur le seg père */

  double rel=dRandUnif();  /* definira la position relative sur le segment */
  origineFils[0]=(rel*segPere->posO[0]) + ((1.0-rel)*segPere->posE[0]);
  origineFils[1]=(rel*segPere->posO[1]) + ((1.0-rel)*segPere->posE[1]);
  origineFils[2]=(rel*segPere->posO[2]) + ((1.0-rel)*segPere->posE[2]);

} /* Fonction origineTard */
/****************************************************************************/
/****************************************************************************/
void origineRamif(pTAxe axePere, r3 origineFils)
{   /* Calcule la position du point d'origine d'une ramification */
origineFils[0]=axePere->pointe->coord[0]-
                  (axePere->pointe->distPrimInit*axePere->pointe->dirCroiss[0]);
origineFils[1]=axePere->pointe->coord[1]-
                  (axePere->pointe->distPrimInit*axePere->pointe->dirCroiss[1]);
origineFils[2]=axePere->pointe->coord[2]-
                  (axePere->pointe->distPrimInit*axePere->pointe->dirCroiss[2]);
} /* Fonction origineRamif */
/****************************************************************************/
/****************************************************************************/
void orienteRamif(pTAxe axePere, r3 dirFils)
{   /* Calcule la direction d'un axe fils issu de ramification */
r3 vAxeRot,rotDirCroiss;
double norVProjHor,angRot;

/* Calcul de la norme de la projection direction sur plan horizontal */
norVProjHor=sqrt((axePere->pointe->dirCroiss[0]*axePere->pointe->dirCroiss[0])+
                 (axePere->pointe->dirCroiss[1]*axePere->pointe->dirCroiss[1]));
if (norVProjHor<epsilon)
{
  vAxeRot[0]=1.0; /* Vecteur initial vertical */
  vAxeRot[1]=0.0;
  vAxeRot[2]=0.0; /* Vecteur (1,0,0) choisi pour axe de rotation */
}
else
{
  vAxeRot[0]=axePere->pointe->dirCroiss[1]/norVProjHor;
  vAxeRot[1]=-axePere->pointe->dirCroiss[0]/norVProjHor;
  vAxeRot[2]=0.0;
}
/* On fait tourner dirCroiss autour de vAxeRot d'un angle d'insertion */
angRot=P_angLat;
rotVect(angRot,vAxeRot,axePere->pointe->dirCroiss,rotDirCroiss);

/* On fait tourner rotDirCroiss autour de dirCroiss d'un angle radial */
angRot=tireAngRad();
rotVect(angRot,axePere->pointe->dirCroiss,rotDirCroiss,dirFils);
} /* Fonction orienteRamif */
/****************************************************************************/
/****************************************************************************/
void ramifieAxe(pTAxe axePere)
{
pTAxe nouvAxe;
float diamRamif;
r3 origRamif, dirRamif;

  /* Décrémente la distance au dernier primordium initié */
  axePere->pointe->distPrimInit-=distInterRamifPointe(axePere->pointe,sol);

  /* Calcul des attributs d'une ramification */
  diamRamif=tireDiamPointeFille(axePere);    /* Tire le diamètre de sa pointe */

  if (diamRamif > P_diamMin)
  {
    origineRamif(axePere,origRamif);         /* Calcule sa position */
    orienteRamif(axePere,dirRamif);          /* Calcule sa direction */

    nouvAxe=initialiseAxe(sR->nbAxeForm+1,diamRamif,origRamif,dirRamif,axePere,axePere->dernSeg);

    ajouteAxeSR(sR,nouvAxe);

  }

} /* Fonction ramifieAxe */
/****************************************************************************/
/****************************************************************************/
void developpeAxe(pTAxe axe,float taux)
/* Assure le développement de l'axe, avec différentes composantes */
{
double elongation;
pTSeg nouvSeg;

  if ((!axe->pointe->senile) && (axe->pointe->mature) && (axe->pointe->diametre>P_diamMin))
  {
    elongation=taux*calcElongationPointe(axe->pointe,sol);

    axe->pointe->longueur+=elongation;

    while (axe->pointe->longueur > longSegNorm) { // on fait un segment "normal"

      axe->pointe->dateDerniereCreation=temps;

      axe->pointe->longueur-=longSegNorm;

      /* Calcule et affecte la nouvelle direction de croissance du méristème */
      reorientePointe(axe->pointe,longSegNorm);

      /* Le méristème se déplace */
      deplacePointe(axe->pointe,longSegNorm);

      if (axe->dernSeg->complet) {
        /* Il génère un nouveau segment sur cet axe à sa nouvelle position */
        increNbSegSR(sR);
        nouvSeg=initialiseSeg(sR->nbSegForm,axe->dernSeg->posE,axe->pointe->coord,axe->pointe->diametre,axe,1,axe->dernSeg);
        ajouteSegProlongeAxe(axe,nouvSeg);
      }
      else { // le premier segment est incomplet, on le modifie
        axe->dernSeg->complet=1;
        axe->dernSeg->posE[0]=axe->pointe->coord[0];
        axe->dernSeg->posE[1]=axe->pointe->coord[1];
        axe->dernSeg->posE[2]=axe->pointe->coord[2];
        axe->dernSeg->jourForm=temps;
      }
//      printf(" Dans developpeAxe\n");

      while (axeRamifiable(axe)) ramifieAxe(axe); // on ramifie éventuellement

    } // fin du while

    if (((temps - axe->pointe->dateDerniereCreation) > dureeSansCreation)&&(axe->pointe->longueur > longSegMin)) { /* segment court  */

      axe->pointe->dateDerniereCreation=temps;


      /* Calcule et affecte la nouvelle direction de croissance de la pointe */
      reorientePointe(axe->pointe,axe->pointe->longueur);

      /* La pointe se déplace */
      deplacePointe(axe->pointe,axe->pointe->longueur);

      /* Elle génère un nouveau segment sur cet axe à sa nouvelle position */
      if (axe->dernSeg->complet) {
        /* Il génère un nouveau segment sur cet axe à sa nouvelle position */
        increNbSegSR(sR);
        nouvSeg=initialiseSeg(sR->nbSegForm,axe->dernSeg->posE,axe->pointe->coord,axe->pointe->diametre,axe,1,axe->dernSeg);
        ajouteSegProlongeAxe(axe,nouvSeg);
      }
      else { // le premier segment est incomplet, on le modifie
        axe->dernSeg->complet=1;
        axe->dernSeg->posE[0]=axe->pointe->coord[0];
        axe->dernSeg->posE[1]=axe->pointe->coord[1];
        axe->dernSeg->posE[2]=axe->pointe->coord[2];
        axe->dernSeg->jourForm=temps;
      }

      axe->pointe->longueur=0.0; // remet la longueur en attente du méristème à 0

      while (axeRamifiable(axe)) ramifieAxe(axe); // on ramifie éventuellement

    } // fin du if

  } // fin du if ((!axe->Pointe->senile) && (axe->Pointe->mature).....

} /* Fonction developpeAxe */
/****************************************************************************/
/****************************************************************************/
void calcTSatisMoySR(pTSysRac sR)
{
/* Calcul du taux de satisfaction moyen sur la période écoulée (de 1 à temps) */

  double tSatisCum=0.0;

  for (int date=1; date<=temps; date++) /* Boucle sur la période écoulée */
  {
    tSatisCum+=sR->tSatis[date];
  }
  sR->tSatisMoy=tSatisCum/temps;

}  /* Fonction calcTSatisMoySR */
/****************************************************************************/
/****************************************************************************/
float calcTauxSatis(float volDemande, float volDisponible)
{
float taux;

  calcTSatisMoySR(sR);

  if (sR->tSatisMoy<=0.7) return 0.0;  // sert à réduire quand demande trop forte

  else {
    if (volDemande==0.0) { taux=1.0; }
    else {
      taux=volDisponible/volDemande;
      /*MODIFBEN monitoring taux satisfaction*/
      printf("\nLe taux de satisfaction avant correction est de %f \n", taux);
      if (taux>1.0) taux=1.0;
    }
    return taux;
  }

} /* Fonction calcTauxSatis */
/****************************************************************************/
/****************************************************************************/
double calcDemandeVolume(pTAxe axe)
{
/* Calcule la demande en volume correspondant à la croissance en longueur
   pour un axe donné */

  return pi*(axe->pointe->diametre)*(axe->pointe->diametre)*calcElongationPointe(axe->pointe,sol)/4.0;

} /* Fonction calcDemandeVolume */
/****************************************************************************/
/****************************************************************************/
void detruitAxe(pTAxe axeADetruire)
/* Supprime un axe en supprimant ses segments, puis l'axe lui-même */
{
pTSeg segCour, segAEnlever;

  /* Libérer tous les segments de cet axe */
  segCour=axeADetruire->premSeg;
  while (segCour->suiv!=NULL)
  {
    segAEnlever=segCour;
    segCour=segCour->suiv;
//    if (ndCour->suivSPere!=NULL) { printf("Problème : Axe ramifié à enlever\n"); exit(1); }
    detruitSeg(segAEnlever);
  }
  detruitSeg(segCour); /* Enlève le segment apical */

  detruitPointe(axeADetruire->pointe);

  /* Enlever l'axe en mémoire */
  free(axeADetruire);

} /* Fonction detruitAxe */
/****************************************************************************/
/****************************************************************************/
int axeToutNecrose(pTAxe axe)
/* Cette fonction retourne la valeur 1 si l'axe a tous ses segments nécrosés et 0 sinon */
{
pTSeg segCour;
int resu=1; // on initialise la valeur résultat à vrai (1)

  if (!axe->pointe->senile) resu=0; // non tout nécrosé si pointe non sénile
  segCour=axe->premSeg;
  while (segCour!=NULL) {
    if (!segCour->necrose) resu=0;  // non tout nécrosé si un segment non nécrosé
    segCour=segCour->suiv;
  }
  return resu;

} /* Fonction axeToutNecrose */
/****************************************************************************/
/****************************************************************************/
void affecValNecroseAxe(pTAxe axe, int valNecrose)
/* Cette fonction affecte à chacun des segments de l'axe
   la valeur de necrose (0 ou 1) */
{
pTSeg segCour;

  segCour=axe->premSeg;
  while (segCour!=NULL)
  {
    segCour->necrose=valNecrose;
    segCour=segCour->suiv;
  }  // fin du while

} /* Fonction affecValNecroseAxe */
/****************************************************************************/
/****************************************************************************/
void affecValNecroseAmont(pTAxe axe, int valNecrose)
/* Cette fonction affecte a chacun des segments en amont de l'axe
la valeur de necrose (0 ou 1) */
{
pTSeg segCour;

  segCour=axe->dernSeg;
  while (segCour!=NULL)
  {
    segCour->necrose=valNecrose;
    segCour=segCour->prec;
  }

} /* Fonction affecValNecroseAmont */
/****************************************************************************/
/****************************************************************************/
void affecValDiamAxe(pTAxe axe, float diam)
/* Cette fonction affecte à chacun des segments de l'axe
   la valeur de diamètre diam */
{
pTSeg segCour;

  segCour=axe->premSeg;
  while (segCour!=NULL)
  {
    segCour->diametre=diam;
    segCour=segCour->suiv;
  }  // fin du while

} /* Fonction affecValDiamAxe */
/****************************************************************************/
/****************************************************************************/
void increValDiamAmont(pTAxe axe, double diam, double coeff)
/* Cette fonction incremente le diametre de chacun des noeuds en amont de l'axe */
{
pTSeg segCour;
double section,diamInit;

  segCour=axe->premSeg->prec; // segment duquel l'axe est segment latéral
  while (segCour!=NULL)
  {
    diamInit=segCour->diametre;
    section=(pi*diamInit*diamInit/4.0)+(pi*coeff*diam*diam/4.0);
    segCour->diametre=sqrt(4.0*section/pi);
    segCour=segCour->prec;
  } // fin du while

} /* Fonction increValDiamAmont */
/****************************************************************************/
/****************************************************************************/
pTSysRac creeSR(void)
/* Cette fonction retourne une nouvelle variable de type pTSysRac,
  c'est-à-dire un pointeur sur le type SysRac */
{
pTSysRac sR;
  sR=(pTSysRac) malloc(sizeof(SysRac));
  if (sR==NULL)
  { printf("Problème mémoire allocation dans CreeSR \n"); exit(1); }

return sR;
} /* Fonction creeSR */
/****************************************************************************/
/****************************************************************************/
void enleveAxeSR(pTSysRac sR, pTAxe axeAEnlever)
/* Cette fonction enlève un axe dans la chaîne des axes du système racinaire */
{
unsigned char axeDestructible=0;

  if (sR->premAxe==NULL)  /* Le système racinaire est vide */
  {
    printf("ATTENTION, probleme dans enleveAxeSR, sR vide \n");
    exit(1);
  }
  else
  {
    if ((axeAEnlever->precedent!=NULL)&&(axeAEnlever->suivant!=NULL)) {
      // On pourra le supprimer, on refait le chaînage
      axeAEnlever->precedent->suivant=axeAEnlever->suivant;
      axeAEnlever->suivant->precedent=axeAEnlever->precedent;
      axeDestructible=1;
    } // fin du if !=NULL && !=NULL

    if ((axeAEnlever->precedent==NULL)&&(axeAEnlever->suivant!=NULL)) {
      // On pourra le supprimer, on refait le chaînage
      axeAEnlever->suivant->precedent=NULL;
      sR->premAxe=axeAEnlever->suivant;
      axeDestructible=1;
    } // fin du if ==NULL && !=NULL

    if ((axeAEnlever->precedent!=NULL)&&(axeAEnlever->suivant==NULL)) {
      // On pourra le supprimer, on refait le chaînage
      axeAEnlever->precedent->suivant=NULL;
      sR->dernAxe=axeAEnlever->precedent;
      axeDestructible=1;
    } // fin du if !=NULL && ==NULL

    if ((axeAEnlever->precedent==NULL)&&(axeAEnlever->suivant==NULL)) {
      // On ne pourra pas le supprimer, car il est seul
      axeDestructible=0;
    } // fin du if ==NULL && ==NULL

    if (axeDestructible) {
      sR->nbAxeSup++;
      detruitAxe(axeAEnlever); // Détruit ses segments, sa pointe, et lui-même
    }
  }
} /* Fonction enleveAxeSR */
/****************************************************************************/
/****************************************************************************/
pTSysRac initialiseSR(r3 origine)
{
/* Initialisation du système racinaire */

pTSysRac sR;

  sR=creeSR();  /* Création d'un système racinaire */

  sR->nbAxeForm=0;  /* Initialisation des variables */
  sR->nbAxeSup=0;
  sR->nbSegForm=0;
  sR->nbPrim=0;
  sR->premAxe=NULL;
  sR->dernAxe=NULL;
  sR->tSatisMoy=1;

  sR->origine[0]=origine[0];  /* Origine du système racinaire */
  sR->origine[1]=origine[1];
  sR->origine[2]=origine[2];

  sR->angDep=2.0*pi*dRandUnif2();  /* Orientation */

  for (int i=0; i<NBPASMAX; i++) sR->tSatis[i]=1.0;

  return(sR);
}  /* Fonction initialiseSR */
/****************************************************************************/
/****************************************************************************/
float longSeg(pTSeg seg)
/* Calcule la longueur d'un segment */
{
  return sqrt(((seg->posE[0]-seg->posO[0])*(seg->posE[0]-seg->posO[0]))+
              ((seg->posE[1]-seg->posO[1])*(seg->posE[1]-seg->posO[1]))+
              ((seg->posE[2]-seg->posO[2])*(seg->posE[2]-seg->posO[2])));

}  /* Fonction longSeg */
/****************************************************************************/
/****************************************************************************/
int calcNouvNbPrim(void)
{
/* Calcul du nouveau nombre de primaires */

  int nouvNbPrim;

  nouvNbPrim=int (P_vitEmissionPrim*temps);

  if (nouvNbPrim>=P_nbMaxPrim) nouvNbPrim=P_nbMaxPrim;

  return nouvNbPrim;

}  /* Fonction calcNouvNBPrim */
/****************************************************************************/
/****************************************************************************/
int calcNouvNbTard(void)
{
/* Calcul du nouveau nombre de racines tardives */

  int nouvNbTard;

  nouvNbTard=int (P_vitEmissionTard*(temps-P_ageEmissionTard));

  if (nouvNbTard>P_nbMaxTard) nouvNbTard=P_nbMaxTard;

  return nouvNbTard;

}  /* Fonction calcNouvNbTard */
/****************************************************************************/
/****************************************************************************/
void emissionPrimSR(pTSysRac sR)
{
/* Emission de nouveaux axes primaires sur le système racinaire */

  pTAxe nouvAxe;
  int numPrim, nbPrimAEmettre;
  r3 vInit, dirInit;
  double angRot,angI;

/*MODIFBEN : si P_simultEmiss est défini à 1, forçage de l'émission de la primaire à T=1
et obligation d'émettre toutes les racines primaires restantes en même temps (cas particulier maïs)*/
  if (P_simultEmiss == 1)
  {
    if (temps == 2)
    {
      //nbPrimAEmettre=1;
      angI=tireGaussien(0.0,0.1); // émission de la radicule qui a un gravitropisme initial fort
      vInit[0]=sin(angI);
      vInit[1]=0.0;
      vInit[2]=cos(angI);
      angRot=sR->angDep+tireAngRad();
      rotZ(vInit,dirInit,angRot);

      /* Génération de l'axe et intégration dans le système racinaire */
      nouvAxe=initialiseAxe(sR->nbAxeForm+1,P_diamMax,sR->origine,dirInit,NULL,NULL);
      ajouteAxeSR(sR,nouvAxe);
      sR->nbPrim++;
    }
    else
    {
      nbPrimAEmettre=calcNouvNbPrim() - sR->nbPrim; /* Nombre de primaires à émettre */
    }
  }
  else
  {
    nbPrimAEmettre=calcNouvNbPrim() - sR->nbPrim; /* Nombre de primaires à émettre */
  }

  if (P_simultEmiss == 1) // Si paramètre d'émission simultanée actif
  {
    if (nbPrimAEmettre == P_nbSeminales) // Si le nombre de racines à émettre est égal au nombre de séminales
    {
      for ((numPrim=1); (numPrim<=nbPrimAEmettre); (numPrim++)) /* Pour les nouvelles primaires à émettre */
      {
      //    printf("Je suis dans emissionPrimSR %3i \n",sR->nbPrim);
      /* Calcul de la direction initiale de l'axe */
      if (sR->nbPrim==0) angI=tireGaussien(0.0,0.1); // émission de la radicule qui a un gravitropisme initial fort
      else angI=tireGaussien(P_angInitMoyVertPrim,P_angInitETVertPrim) + (temps * P_slopePrimAngle) ; // angle par rapport à la verticale
      vInit[0]=sin(angI);
      vInit[1]=0.0;
      vInit[2]=cos(angI);
      angRot=sR->angDep+tireAngRad();
      rotZ(vInit,dirInit,angRot);

      /* Génération de l'axe et intégration dans le système racinaire */
      nouvAxe=initialiseAxe(sR->nbAxeForm+1,P_diamMax,sR->origine,dirInit,NULL,NULL);
      ajouteAxeSR(sR,nouvAxe);
      sR->nbPrim++;
      P_nbSeminales++;
      }
    }
  }
  else
  {
    for ((numPrim=1); (numPrim<=nbPrimAEmettre); (numPrim++)) /* Pour les nouvelles primaires à émettre */
    {
  //    printf("Je suis dans emissionPrimSR %3i \n",sR->nbPrim);
      /* Calcul de la direction initiale de l'axe */
      if (sR->nbPrim==0) angI=tireGaussien(0.0,0.1); // émission de la radicule qui a un gravitropisme initial fort
        else angI=tireGaussien(P_angInitMoyVertPrim,P_angInitETVertPrim); // angle par rapport à la verticale
      vInit[0]=sin(angI);
      vInit[1]=0.0;
      vInit[2]=cos(angI);
      angRot=sR->angDep+tireAngRad();
      rotZ(vInit,dirInit,angRot);

      /* Génération de l'axe et intégration dans le système racinaire */
      nouvAxe=initialiseAxe(sR->nbAxeForm+1,P_diamMax,sR->origine,dirInit,NULL,NULL);
      ajouteAxeSR(sR,nouvAxe);
      sR->nbPrim++;
    }
  }


}  /* Fonction emissionPrimSR */
/****************************************************************************/
/****************************************************************************/
void emissionTardSR(pTSysRac sR)
{
/* Emission de nouveaux axes tardifs sur le système racinaire */

  pTAxe nouvAxe;
  pTSeg segPere;  /* Segment sur lequel la racine tardive sera émise */
  int numTard, nbTardAEmettre;
  r3 vInit, dirInit, posInit;
  double angRot,angI,dBaseTard,dBaseCour;

  nbTardAEmettre=calcNouvNbTard() - sR->nbTard; /* Nombre de racines tardives à émettre */
  for ((numTard=1); (numTard<=nbTardAEmettre); (numTard++)) /* Pour les nouvelles tardives à émettre */
  {
//    printf("Je suis dans emissionTardSR %3i \n",sR->nbTard);
	/* Calcul de la position initiale de l'axe */
	  /* Tirage de la distance à la base de cette tardive */
	  dBaseTard=dRandUnif()*P_dBaseMaxTard;

	  /* Détermination du segment père, sur le premier axe */
	  segPere=sR->premAxe->premSeg;
	  dBaseCour=longSeg(segPere);
	  while ((dBaseCour < dBaseTard) && (segPere->suiv!=NULL)) {
		segPere=segPere->suiv;
		dBaseCour+=longSeg(segPere);
	  }

	  /* Position sur ce segment */
	  origineTard(segPere,posInit);
    /* Calcul de la direction initiale de l'axe */
    angI=tireGaussien(P_angInitMoyVertTard,P_angInitETVertTard); // angle par rapport à la verticale
    vInit[0]=sin(angI);
    vInit[1]=0.0;
    vInit[2]=cos(angI);
    angRot=tireAngRad();
    rotZ(vInit,dirInit,angRot);

    /* Génération de l'axe et intégration dans le système racinaire */
    nouvAxe=initialiseAxe(sR->nbAxeForm+1,P_propDiamTard*P_diamMax,posInit,dirInit,sR->premAxe,segPere);
    ajouteAxeSR(sR,nouvAxe);
    sR->nbPrim++;
  }

  }  /* Fonction emissionTardSR */
/****************************************************************************/
/****************************************************************************/
void calcVolProdSR(pTSysRac sR)
{
/* Calcul du volume racinaire produit sur la période écoulée */
  int date;

  sR->volProd=0.0;
  for ((date=1); (date<=temps); (date++)) /* Boucle sur la période écoulée */
  {
    sR->volProd+=sR->volDem[date]*sR->tSatis[date];
  }

}  /* Fonction calcVolProdSR */
/****************************************************************************/
/****************************************************************************/
void litParam(void)

/* Fonction de lecture des parametres de la simulation */
{
char bid[MAXLINE];

  // Durée de simulation
  fscanf(FPar,"%i",&P_duree);
  fgets(bid,MAXLINE-1,FPar); // reste de la ligne

  /*MODIFBEN lecture du switch pour l'emission simultanée de séminales*/
  fscanf(FPar,"%i",&P_simultEmiss);
  fgets(bid,MAXLINE-1,FPar); // reste de la ligne

  fscanf(FPar,"%f",&P_vitEmissionPrim);
  fgets(bid,MAXLINE-1,FPar); // reste de la ligne

  /*MODIFBEN lecture du nombre de séminales*/
  fscanf(FPar,"%i",&P_nbSeminales);
  fgets(bid,MAXLINE-1,FPar); // reste de la ligne

  fscanf(FPar,"%i",&P_nbMaxPrim);
  fgets(bid,MAXLINE-1,FPar); // reste de la ligne

  fscanf(FPar,"%f",&P_diamMin);
  fgets(bid,MAXLINE-1,FPar); // reste de la ligne

  fscanf(FPar,"%f",&P_diamMax);
  fgets(bid,MAXLINE-1,FPar); // reste de la ligne
  P_diamMax = P_diamMax ;// dRandUnif(); // #ModifGui -> Add variations in the ramification

  fscanf(FPar,"%f",&P_penteVitDiam);
  fgets(bid,MAXLINE-1,FPar); // reste de la ligne

  fscanf(FPar,"%f",&P_intensiteTropisme);
  fgets(bid,MAXLINE-1,FPar); // reste de la ligne


  fscanf(FPar,"%f",&P_distRamif);
  fgets(bid,MAXLINE-1,FPar); // reste de la ligne
  P_distRamif = P_distRamif;// * dRandUnif(); // #ModifGui -> Add variations in the ramification

  fscanf(FPar,"%f",&P_propDiamRamif);
  fgets(bid,MAXLINE-1,FPar); // reste de la ligne

  fscanf(FPar,"%f",&P_coeffVarDiamRamif);
  fgets(bid,MAXLINE-1,FPar); // reste de la ligne

  fscanf(FPar,"%f",&P_probaMaxArret);
  fgets(bid,MAXLINE-1,FPar); // reste de la ligne


  fscanf(FPar,"%f",&P_TMD);
  fgets(bid,MAXLINE-1,FPar); // reste de la ligne

  fscanf(FPar,"%f",&P_penteDureeVieDiamTMD);
  fgets(bid,MAXLINE-1,FPar); // reste de la ligne

  fscanf(FPar,"%f",&P_coeffCroissRad);
  fgets(bid,MAXLINE-1,FPar); // reste de la ligne

  fscanf(FPar,"%f",&P_angLat);
  fgets(bid,MAXLINE-1,FPar); // reste de la ligne

  fscanf(FPar,"%i",&P_tertiary);
  fgets(bid,MAXLINE-1,FPar); // reste de la ligne

  fscanf(FPar,"%s[",&P_outputName);
  fgets(bid,MAXLINE-1,FPar); // reste de la ligne

  fscanf(FPar,"%i",&P_type); // Type of simulation (1 = 3D, 2 = 2D, 3 = shovelomics)
  fgets(bid,MAXLINE-1,FPar);

  fscanf(FPar,"%f",&P_IC_meca);
  fgets(bid,MAXLINE-1,FPar); // Mecanical impedence

  fscanf(FPar,"%i",&P_shovel); // Depth of the shovelomics
  fgets(bid,MAXLINE-1,FPar);

  fscanf(FPar,"%f",&P_maxLatAge); // Max growing age for the laterals
  fgets(bid,MAXLINE-1,FPar);

  fscanf(FPar,"%f",&P_angInitMoyVertPrim); // Angle for the primary emission
  fgets(bid,MAXLINE-1,FPar);

  fscanf(FPar,"%f",&P_slopePrimAngle); // Slope for angle for the primary emission
  fgets(bid,MAXLINE-1,FPar);

  if (P_nbMaxPrim==1) {
    P_angInitMoyVertPrim=0.0; /* la racine émise est proche de la verticale */
    P_angInitETVertPrim=0.05;  /* écart-type de l'angle d'insertion  */
  }
} /* Fonction litParam */
/****************************************************************************/
/****************************************************************************/
void origineEmission(pTAxe nouvAxe)
{
nouvAxe->pointe->coord[0]=sR->origine[0];
nouvAxe->pointe->coord[1]=sR->origine[1];
nouvAxe->pointe->coord[2]=sR->origine[2];
} /* Fonction origineEmission */
/****************************************************************************/
/****************************************************************************/
void orienteEmission(pTAxe nouvAxe, int num)
{
double angRot,angI;
r3 vInit;

angI=tireGaussien(P_angInitMoyVertPrim,P_angInitETVertPrim);
vInit[0]=sin(angI);
vInit[1]=0.0;
vInit[2]=cos(angI);

angRot=sR->angDep+(2*pi*num/P_nbMaxPrim);
rotZ(vInit,nouvAxe->pointe->dirCroiss,angRot);
} /* Fonction orienteEmission */
/****************************************************************************/
/****************************************************************************/
float volPrimSeg(pTSeg seg)
/* Calcule le volume primaire du segment */
{
  return 0.25*pi*seg->axe->pointe->diametre*seg->axe->pointe->diametre*
  sqrt(((seg->posE[0]-seg->posO[0])*(seg->posE[0]-seg->posO[0]))+
       ((seg->posE[1]-seg->posO[1])*(seg->posE[1]-seg->posO[1]))+
       ((seg->posE[2]-seg->posO[2])*(seg->posE[2]-seg->posO[2])));

}  /* Fonction volSeg */
/****************************************************************************/
/****************************************************************************/
float volTotalSeg(pTSeg seg)
/* Calcule le volume total du segment */
{
  return 0.25*pi*seg->diametre*seg->diametre*sqrt(((seg->posE[0]-seg->posO[0])*(seg->posE[0]-seg->posO[0]))+
                                                  ((seg->posE[1]-seg->posO[1])*(seg->posE[1]-seg->posO[1]))+
                                                  ((seg->posE[2]-seg->posO[2])*(seg->posE[2]-seg->posO[2])));

}  /* Fonction volSeg */
/****************************************************************************/
/****************************************************************************/
float distHorSeg(pTSeg seg)
/* Calcule la distance horizontale d'un segment */
{
  return sqrt(((seg->posE[0]+seg->posO[0])*(seg->posE[0]+seg->posO[0])/4)+
              ((seg->posE[1]+seg->posO[1])*(seg->posE[1]+seg->posO[1])/4));

}  /* Fonction distHorSeg */
/****************************************************************************/
/****************************************************************************/
void calcLimitesSR(pTSysRac sR)
{
/* Calcul des limites du système racinaire et de quelques autres variables */
  pTAxe axeCour;
  pTSeg segCour;
  float distHor,distHorLong,profLong,longS,profS,amplMax;

  // Initialisation des variables
  sR->volPrim=0.0;  // volume des structures primaires
  sR->secPointe=0.0;  // section totale des pointes actives
  sR->volTot=0.0;   // volume total
  sR->longueur=0.0;  // longueur
  sR->diamMax=-1.0e10; // diamètre maximal, du plus gros segment
  sR->distMax=-1.0e10;  // extension maximale
  sR->profMax=-1.0e10;  // profondeur maximale

  sR->xbinf=+1.0e10; sR->ybinf=+1.0e10; sR->zbinf=+1.0e10; // initialisation des valeurs
  sR->xbsup=-1.0e10; sR->ybsup=-1.0e10; sR->zbsup=-1.0e10;

  distHorLong=0.0;
  profLong=0.0;

  axeCour=sR->premAxe;
  while (axeCour!=NULL)  // Calcul du volume "demandé"
  {
    sR->volTot+=pi*axeCour->pointe->diametre*axeCour->pointe->diametre*axeCour->pointe->longueur/4;
    sR->volPrim+=pi*axeCour->pointe->diametre*axeCour->pointe->diametre*axeCour->pointe->longueur/4;
    if ((axeCour->pointe->mature)&&(!axeCour->pointe->senile))
      sR->secPointe+=pi*axeCour->pointe->diametre*axeCour->pointe->diametre/4;
    segCour=axeCour->premSeg;
    while (segCour!=NULL) { // Tant que ce segment existe
      // Calculs sur le segment courant segCour
      if (segCour->posO[0] < sR->xbinf) { sR->xbinf=segCour->posO[0]; }
      if (segCour->posE[0] < sR->xbinf) { sR->xbinf=segCour->posE[0]; }
      if (segCour->posO[0] > sR->xbsup) { sR->xbsup=segCour->posO[0]; }
      if (segCour->posE[0] > sR->xbsup) { sR->xbsup=segCour->posE[0]; }

      if (segCour->posO[1] < sR->ybinf) { sR->ybinf=segCour->posO[1]; }
      if (segCour->posE[1] < sR->ybinf) { sR->ybinf=segCour->posE[1]; }
      if (segCour->posO[1] > sR->ybsup) { sR->ybsup=segCour->posO[1]; }
      if (segCour->posE[1] > sR->ybsup) { sR->ybsup=segCour->posE[1]; }

      if (segCour->posO[2] < sR->zbinf) { sR->zbinf=segCour->posO[2]; }
      if (segCour->posE[2] < sR->zbinf) { sR->zbinf=segCour->posE[2]; }
      if (segCour->posO[2] > sR->zbsup) { sR->zbsup=segCour->posO[2]; }
      if (segCour->posE[2] > sR->zbsup) { sR->zbsup=segCour->posE[2]; }

      if (segCour->diametre > sR->diamMax) { sR->diamMax=segCour->diametre; }

      distHor=distHorSeg(segCour);
      if (distHor > sR->distMax) { sR->distMax=distHor; }

      if (segCour->posO[2]>segCour->posE[2]) profS=segCour->posO[2]; else profS=segCour->posE[2];
      if (profS > sR->profMax) { sR->profMax=profS; }

      sR->volTot+=volTotalSeg(segCour);
      sR->volPrim+=volPrimSeg(segCour);
      longS=longSeg(segCour);
      sR->longueur+=longS;
      distHorLong+=distHor*longS;
      profLong+=profS*longS;

      segCour=segCour->suiv;
    }  // fin du while segCour
    axeCour=axeCour->suivant;
  }  // fin du while axeCour

  sR->xbinf=sR->xbinf-d2; sR->xbsup=sR->xbsup+d2;
  sR->ybinf=sR->ybinf-d2; sR->ybsup=sR->ybsup+d2;
  sR->zbinf=sR->zbinf-d2; sR->zbsup=sR->zbsup+d2;

  // Calcul de la maille de sol, en fonction de l'amplitude à balayer

  amplMax=0.0;
  if ((sR->xbsup-sR->xbinf) > amplMax) amplMax=sR->xbsup-sR->xbinf;
  if ((sR->ybsup-sR->ybinf) > amplMax) amplMax=sR->ybsup-sR->ybinf;
  if ((sR->zbsup-sR->zbinf) > amplMax) amplMax=sR->zbsup-sR->zbinf;

  maille=amplMax/(NBCASEMAX-1);
  if (maille<mailleMin) maille=mailleMin;
  volElemSol=maille*maille*maille;

//  maille=5.0; volElemSol=maille*maille*maille;

  sR->distMoy=distHorLong/sR->longueur;
  sR->profMoy=profLong/sR->longueur;

//  printf(" xbinf :%7.2f",sR->xbinf); printf(" xbsup :%7.2f\n",sR->xbsup);
//  printf(" ybinf :%7.2f",sR->ybinf); printf(" ybsup :%7.2f\n",sR->ybsup);
//  printf(" zbinf :%7.2f",sR->zbinf); printf(" zbsup :%7.2f\n",sR->zbsup);
//  printf(" amplMax :%7.2f",amplMax); printf(" maille :%7.2f\n",maille);


}  /* Fonction calcLimitesSR */
/****************************************************************************/
/*************************************************************************/
void translateSR(pTSysRac sR)
/* Translate le système racinaire de façon à ce que tout se passe en territoire
 positif et démarre de 0*/

{
pTAxe axeCour;
pTSeg segCour;

  axeCour=sR->premAxe;
  while (axeCour!=NULL)  // Calcul du volume "demandé"
  {
    // Translation de la pointe de l'axe
    axeCour->pointe->coord[0] -= sR->xbinf;
    axeCour->pointe->coord[1] -= sR->ybinf;
    axeCour->pointe->coord[2] -= sR->zbinf;

    segCour=axeCour->premSeg;
    while (segCour!=NULL) { // Tant qu'il y a des segments sur l'axe
      // Translation du segment segCour
      segCour->posO[0] -= sR->xbinf;
      segCour->posO[1] -= sR->ybinf;
      segCour->posO[2] -= sR->zbinf;

      segCour->posE[0] -= sR->xbinf;
      segCour->posE[1] -= sR->ybinf;
      segCour->posE[2] -= sR->zbinf;

      segCour=segCour->suiv;
    }
    axeCour=axeCour->suivant;
  }

  sR->xbsup-=sR->xbinf; sR->ybsup-=sR->ybinf; sR->zbsup-=sR->zbinf;
  sR->xbinf=0.0; sR->ybinf=0.0; sR->zbinf=0.0;

} /* Fonction translateSR */
/*************************************************************************/
/*************************************************************************/
void initialiseTabSol(void)
/* Initialise le tableau sol. La valeur 3 signifie que le point est à
   distance supérieure à d1 et d2 */
{
  for (int i=0; i<=NBCASEMAX; i++) { // pour le sol, on initialise à 3
    for (int j=0; j<=NBCASEMAX; j++) {
      for (int k=0; k<=NBCASEMAX; k++) { vox[i][j][k]=3; }
    }
  }

} /* Fonction initialiseTabSol */
/*************************************************************************/
/*************************************************************************/
int rangCase(float coord)
{ // renvoie le rang de la case du tableau des voxels dans laquelle est cette coordonnée

  int rang=int (coord/maille);
  if (rang<0) return 0; else if (rang>NBCASEMAX) return NBCASEMAX;
  return rang;

} /* Fonction rangCase */
/*************************************************************************/
/*************************************************************************/
float coordPointCase(int rangCase)
{ // renvoie les coordonnées d'un point dans la case

  return (((rangCase+0.5)*maille)+(0.5*maille*dRandUnif2()));

} /* Fonction coordPointCase */
/*************************************************************************/
/*************************************************************************/
float coordCentreCase(int rangCase)
{ // renvoie les coordonnées du centre de la case

  return ((rangCase+0.5)*maille);

} /* Fonction coordCentreCase */
/*************************************************************************/
/*************************************************************************/
void calcDistancesSR(pTSysRac sR)
/* Calcule les distances entre mailles du sol et segments racinaires
   et calcule les volumes colonisés */
{
  pTAxe axeCour;
  pTSeg segCour;

  float xp1,yp1,zp1, xp2,yp2,zp2, dx,dy,dz, dM, distCour,
        xMin, xMax, yMin, yMax, zMin, zMax, xS, yS, zS, xProj, yProj, zProj;
//  float dist1,dist2;   // si besoin

  axeCour=sR->premAxe;
  while (axeCour!=NULL) // Tant qu'il y a des axes dans le système racinaire
  {
    segCour=axeCour->premSeg;
    if (segCour->complet) {
      while (segCour!=NULL)  // Tant qu'il y a des segments sur l'axe
      {
        xp1=segCour->posO[0]; yp1=segCour->posO[1]; zp1=segCour->posO[2];
        xp2=segCour->posE[0]; yp2=segCour->posE[1]; zp2=segCour->posE[2];

        // Calcul des limites du domaine à explorer pour ce segment
        if (xp1<xp2) { xMin=xp1 - d2; xMax=xp2 + d2; }
        else { xMin=xp2 - d2; xMax=xp1 + d2; }
        if (yp1<yp2) { yMin=yp1 - d2; yMax=yp2 + d2; }
        else { yMin=yp2 - d2; yMax=yp1 + d2; }
        if (zp1<zp2) { zMin=zp1 - d2; zMax=zp2 + d2; }
        else { zMin=zp2 - d2; zMax=zp1 + d2; }

          // balayage de ce domaine pertinent et calcul des distances
        for (int caseX=rangCase(xMin); caseX<=rangCase(xMax); caseX++) {
          for (int caseY=rangCase(yMin); caseY<=rangCase(yMax); caseY++) {
            for (int caseZ=rangCase(zMin); caseZ<=rangCase(zMax); caseZ++) {

              xS=coordCentreCase(caseX); yS=coordCentreCase(caseY); zS=coordCentreCase(caseZ);

              // On calcule le projeté du point sol sur la droite contenant p1 et p2
              dx=xp2-xp1; dy=yp2-yp1; dz=zp2-zp1;
              dM=(dx*xS)+(dy*yS)+(dz*zS);

              xProj=((dM*dx)+(xp1*((dy*dy)+(dz*dz)))-(zp1*dx*dz)-(yp1*dx*dy))/((dx*dx)+(dy*dy)+(dz*dz));

              if ((xProj<=xp1 && xProj>=xp2)|(xProj>=xp1 && xProj<=xp2))
              { // Le projeté est entre les deux points du segment
                yProj=((dM*dy)+(yp1*((dx*dx)+(dz*dz)))-(xp1*dx*dy)-(zp1*dz*dy))/((dx*dx)+(dy*dy)+(dz*dz));
                zProj=((dM*dz)+(zp1*((dx*dx)+(dy*dy)))-(yp1*dz*dy)-(xp1*dz*dx))/((dx*dx)+(dy*dy)+(dz*dz));
                distCour=sqrt(((xProj-xS)*(xProj-xS))+((yProj-yS)*(yProj-yS))+((zProj-zS)*(zProj-zS)));
              }  // fin du if
              else
              {  // Le projeté est à l'extérieur du segment
              /*
                dist1=sqrt(((xp1-xS)*(xp1-xS))+((yp1-yS)*(yp1-yS))+
                           ((zp1-zS)*(zp1-zS)));
                dist2=sqrt(((xp2-xS)*(xp2-xS))+((yp2-yS)*(yp2-yS))+
                           ((zp2-zS)*(zp2-zS)));
                if (dist1<dist2) { distCour=dist1; } else { distCour=dist2; }
              */
              distCour=2000.0;
              }  // fin du else

              if ((distCour<=d2)&&(vox[caseX][caseY][caseZ]>2)) { vox[caseX][caseY][caseZ]=2; }
              if ((distCour<=d1)&&(vox[caseX][caseY][caseZ]>1)) { vox[caseX][caseY][caseZ]=1; }

            } // for du for caseZ
          } // for du for caseY
        } // for du for caseX
        segCour=segCour->suiv;
      }  // fin du while (segCour!=NULL)
    } // fin du if (segCour->complet)
    axeCour=axeCour->suivant;
  } // fin du while (axeCour!=NULL)

  sR->volSolD1=0.0;  // initialisation avant cumul
  sR->volSolD2=0.0;  // initialisation avant cumul
  for (int i=0; i<=NBCASEMAX; i++) {
    for (int j=0; j<=NBCASEMAX; j++) {
      for (int k=0; k<=NBCASEMAX; k++) {
        if (vox[i][j][k]==2) {
          sR->volSolD2+=volElemSol;
        } // fin du if
        if (vox[i][j][k]==1) {
          sR->volSolD2+=volElemSol; sR->volSolD1+=volElemSol;
        } // fin du if
      }  // fin du for sur k
    }  // fin du for sur j
  }  // fin du for sur i


}  /* Fonction calcDistancesSR  */
/****************************************************************************/
/****************************************************************************/
void developpeSR(pTSysRac sR)
{
/* Développement : croissance et ramification de chaque axe du système */
pTAxe axeCour;
double volumeDem=0.0;

  axeCour=sR->premAxe;
  while (axeCour!=NULL)  // Calcul du volume "demandé"
  {
    volumeDem+=calcDemandeVolume(axeCour);
    axeCour=axeCour->suivant;
  }
  //fprintf(FDem, "%i,%f \n", temps, volumeDem); // MODIFBEN : imprime dans le fichier FDem (demand.csv) le temps et la demande

  sR->volDem[temps]=volumeDem;
  sR->tSatis[temps]=1; //calcTauxSatis(volumeDem,sR->volMax[temps]);

  axeCour=sR->premAxe;
  while (axeCour!=NULL)  // Développement
  {
    developpeAxe(axeCour,sR->tSatis[temps]); // développe l'axe (croissance, ramif)
    developpePointe(axeCour->pointe);        // modifie les attributs de la pointe
    axeCour=axeCour->suivant;
  }

  // printf(" Volume demandé : %16.5f \n",volumeDem);
  // printf(" NbRac : %6i \n",sR->nbAxeForm);

}  /* Fonction developpeSR */
/****************************************************************************/
/****************************************************************************/
void mortaliteSR(pTSysRac sR)
{
pTAxe axeCour, axeAEnlever;

  // Premier passage : calcul de la sénilité et affectation nécrose sur l'ensemble des axes */
  axeCour=sR->premAxe; // Dans le sens de premiers vers les derniers
  while (axeCour!=NULL)
  {
    if (axeCour->pointe->senile)
    { /* L'axe est nécrosé */
      affecValNecroseAxe(axeCour, 1);
    }
    else
    {  /* L'axe n'est pas nécrosé */
      /* Tous les noeuds en amont de la pointe ne sont pas necrosés non plus */
      affecValNecroseAmont(axeCour, 0);
    }

    axeCour=axeCour->suivant;
  }

  // Calcul de l'élagage, enlèvement des axes tout nécrosés
  axeCour=sR->dernAxe; // Dans le sens de derniers vers les premiers
  while (axeCour!=NULL)
  {
    if (axeToutNecrose(axeCour))
    {
      axeAEnlever=axeCour;
      axeCour=axeCour->precedent;
      if (axeAEnlever->pere!=NULL) enleveAxeSR(sR,axeAEnlever);
    }
    else axeCour=axeCour->precedent;
  }

}  /* Fonction mortaliteSR */
/****************************************************************************/
/****************************************************************************/
void croissanceRadialeSR(pTSysRac sR, float coeffCroiss)
{
pTAxe axeCour;
float diam;

  /* Premier passage, initialisation aux diametres primaires */
  axeCour=sR->dernAxe;
  while (axeCour!=NULL)
  {
    diam=axeCour->pointe->diametre;
    affecValDiamAxe(axeCour, diam);
    axeCour=axeCour->precedent;
  }

  /* Deuxième passage, avec incrément des diametres */
  axeCour=sR->dernAxe;
  while (axeCour!=NULL)
  {
    /* les noeuds en amont sont incrémentés si axe en croissance (pointe mature et non senile) */
    if ((axeCour->pointe->mature)&&(!axeCour->pointe->senile))
    {
      diam=axeCour->pointe->diametre;
      increValDiamAmont(axeCour, diam, coeffCroiss);
    }
    axeCour=axeCour->precedent;
  }

}  /* Fonction croissanceRadialeSR */

/****************************************************************************/
/****************************************************************************/
void imprimeSeg(pTSeg seg, bool last)
/* Imprime un segment sur le fichier des segments */
{
/*
  long int suivant,precedent;

  if (seg->prec==NULL) precedent=0;
  else precedent=seg->prec->num;

  if (seg->suiv==NULL) suivant=0;
  else suivant=seg->suiv->num;

  fprintf(FSeg,"%5li %5i %5li %5li %5li %7.2f %7.2f %7.2f %7.2f %7.2f %7.2f %7.2f\n",
    seg->num,seg->jourForm,seg->axe->num,suivant,precedent,seg->diametre,
    seg->posO[0],seg->posO[1],seg->posO[2],seg->posE[0],seg->posE[1],seg->posE[2]);
*/
//  fprintf(FSeg,"%5li %5i %7.2f %7.2f %7.2f %7.2f %7.2f %7.2f %7.2f\n",
//    seg->axe->num,seg->jourForm,seg->diametre,
//    seg->posO[0],seg->posO[1],seg->posO[2],seg->posE[0],seg->posE[1],seg->posE[2]);
// fprintf(FSeg,"%5li %5i %7.2f %7.2f %7.2f %7.2f %7.2f %7.2f %7.2f\n",
//    seg->axe->num,seg->jourForm,seg->diametre,
//    seg->posO[0],seg->posO[1],seg->posO[2]);

  if(P_type != 3 || (P_type == 3 && seg->posE[2] < P_shovel && seg->posE[1] < P_shovel && seg->posE[0] < P_shovel)){
     if(!last){
          fprintf(FSeg,"             <point x='");
          fprintf(FSeg,"%f",seg->posO[0]*scale);
          fprintf(FSeg,"' y='");
          fprintf(FSeg,"%f",seg->posO[2]*scale);
          fprintf(FSeg,"' z='");
          fprintf(FSeg,"%f",seg->posO[1]*scale);
          fprintf(FSeg,"'/>\n");
      }
       if(last){
          fprintf(FSeg,"             <point x='");
          fprintf(FSeg,"%f",seg->posE[0]*scale);
          fprintf(FSeg,"' y='");
          fprintf(FSeg,"%f",seg->posE[2]*scale);
          fprintf(FSeg,"' z='");
          fprintf(FSeg,"%f",seg->posE[1]*scale);
          fprintf(FSeg,"'/>\n");
      }
  }


}  /* Fonction imprimeSeg */

 /****************************************************************************/
/****************************************************************************/
void printAge(pTSeg seg)
/* Imprime un segment sur le fichier des segments */
{
  if(P_type != 3 || (P_type == 3 && seg->posE[2] < P_shovel && seg->posE[1] < P_shovel && seg->posE[0] < P_shovel)){

    fprintf(FSeg,"             <sample>");
    fprintf(FSeg,"%i", seg->jourForm);
    fprintf(FSeg,"</sample>\n");
  }

}  /* Fonction imprimeSeg */

 /****************************************************************************/
/****************************************************************************/
void printDiam(pTSeg seg)
/* Imprime un segment sur le fichier des segments */
{
  if(P_type != 3 || (P_type == 3 && seg->posE[2] < P_shovel && seg->posE[1] < P_shovel && seg->posE[0] < P_shovel)){

    fprintf(FSeg,"             <sample>");
    fprintf(FSeg,"%f", seg->diametre*scale);
    fprintf(FSeg,"</sample>\n");
  }

}  /* Fonction imprimeSeg */

/****************************************************************************/
/****************************************************************************/
void imprimeSRGlobal(pTSysRac sR)
{

//  fprintf(FSynth,"        longueur           volTot           volPrim          volProd          secPointe          diamMax        tSatisMoy          profMax          profMoy          distMax          distMoy         volSolD1         volSolD2\n");
/*
	fprintf(FSynth,"%16.2f %16.2f %16.2f %16.2f %16.2f %16.2f %16.2f %16.2f %16.2f %16.2f %16.2f\n",
          sR->longueur,sR->volTot,sR->volPrim,sR->volProd,sR->tSatisMoy,sR->profMax,
          sR->profMoy,sR->distMax,sR->distMoy,sR->volSolD1/1000.0,sR->volSolD2/1000.0);
*/
}  /* Fonction imprimeSRGlobal */
/****************************************************************************/
/*************************************************************************/
void imprimeSolColonise(int distance)
/* Imprime les cellules de sol colonisé */
{
/*
  // Impression de l'entête
  fprintf(FVox,"caseX caseY caseZ\n");

  for (int i=0; i<NBCASEMAX; i++) {
    for (int j=0; j<NBCASEMAX; j++) {
      for (int k=0; k<NBCASEMAX; k++) {
        if (vox[i][j][k]<=distance) fprintf(FVox,"%5i %5i %5i\n",i,j,k);
      } // fin du for k
    }   // fin du for j
  }     // fin du for i
*/

} /* Fonction imprimeSolColonise */
/*************************************************************************/
/****************************************************************************/
void imprimeAudit(void)
{/*

  fprintf(FAudit,"voldispo                voldem             tsatis\n");
  for (int pas=1; pas<NBPASMAX; pas++)
  {
    fprintf(FAudit,"%16.2f %16.2f %8.5f\n",sR->volMax[pas],sR->volDem[pas],sR->tSatis[pas]);
  }
*/
}  /* Fonction imprimeAudit */
/****************************************************************************/
/****************************************************************************/
void imprimeSRSegmentsEntete(void)
{   /* Imprime l'entête du fichier contenant les noeuds du système racinaire */
  FSeg=fopen(P_outputName2,"w"); //MODIFBEN passage à outputname2, incluant le pas de temps

 // fprintf(FSeg,"NumSeg Jour NumAxe Suiv Prec Diam     X1       Y1       Z1      X2       Y2       Z2\n");
  //fprintf(FSeg,"NumAxe Jour Diam     X1       Y1       Z1      X2       Y2       Z2\n");
fprintf(FSeg,"<?xml version='1.0' encoding='UTF-8'?>\n");
fprintf(FSeg,"<rsml xmlns:po='http://www.plantontology.org/xml-dtd/po.dtd'>\n");
fprintf(FSeg,"  <metadata>\n");
fprintf(FSeg,"    <version>1</version>\n");
fprintf(FSeg,"    <unit>inch</unit>\n");
fprintf(FSeg,"    <resolution>");
fprintf(FSeg,"%i",dpi );
fprintf(FSeg,"</resolution>\n");
fprintf(FSeg,"    <last-modified>today</last-modified>\n");
fprintf(FSeg,"    <software>archisimple</software>\n");

// Print the parameters of the simulation
fprintf(FSeg,"    <parameters>\n");  

fprintf(FSeg,"      <parameter name='P_duree'>");
fprintf(FSeg,"%i",P_duree);
fprintf(FSeg,"</parameter>\n");

fprintf(FSeg,"      <parameter name='P_simultEmiss'>");
fprintf(FSeg,"%i",P_simultEmiss);
fprintf(FSeg,"</parameter>\n");

fprintf(FSeg,"      <parameter name='P_vitEmissionPrim'>");
fprintf(FSeg,"%f",P_vitEmissionPrim);
fprintf(FSeg,"</parameter>\n");

fprintf(FSeg,"      <parameter name='P_nbSeminales'>");
fprintf(FSeg,"%i",P_nbSeminales);
fprintf(FSeg,"</parameter>\n");

fprintf(FSeg,"      <parameter name='P_nbMaxPrim'>");
fprintf(FSeg,"%i",P_nbMaxPrim);
fprintf(FSeg,"</parameter>\n");

fprintf(FSeg,"      <parameter name='P_ageEmissionTard'>");
fprintf(FSeg,"%f",P_ageEmissionTard);
fprintf(FSeg,"</parameter>\n");

fprintf(FSeg,"      <parameter name='P_dBaseMaxTard'>");
fprintf(FSeg,"%f",P_dBaseMaxTard);
fprintf(FSeg,"</parameter>\n");

fprintf(FSeg,"      <parameter name='P_vitEmissionTard'>");
fprintf(FSeg,"%f",P_vitEmissionTard);
fprintf(FSeg,"</parameter>\n");

fprintf(FSeg,"      <parameter name='P_propDiamTard'>");
fprintf(FSeg,"%f",P_propDiamTard);
fprintf(FSeg,"</parameter>\n");

fprintf(FSeg,"      <parameter name='P_nbMaxTard'>");
fprintf(FSeg,"%i",P_nbMaxTard);
fprintf(FSeg,"</parameter>\n");

fprintf(FSeg,"      <parameter name='P_diamMin'>");
fprintf(FSeg,"%f",P_diamMin);
fprintf(FSeg,"</parameter>\n");

fprintf(FSeg,"      <parameter name='P_diamMax'>");
fprintf(FSeg,"%f",P_diamMax);
fprintf(FSeg,"</parameter>\n");

fprintf(FSeg,"      <parameter name='P_penteVitDiam'>");
fprintf(FSeg,"%f",P_penteVitDiam);
fprintf(FSeg,"</parameter>\n");

fprintf(FSeg,"      <parameter name='P_tendanceDirTropisme'>");
fprintf(FSeg,"%i",P_tendanceDirTropisme);
fprintf(FSeg,"</parameter>\n");

fprintf(FSeg,"      <parameter name='P_intensiteTropisme'>");
fprintf(FSeg,"%f",P_intensiteTropisme);
fprintf(FSeg,"</parameter>\n");

fprintf(FSeg,"      <parameter name='P_ageMaturitePointe'>");
fprintf(FSeg,"%f",P_ageMaturitePointe);
fprintf(FSeg,"</parameter>\n");

fprintf(FSeg,"      <parameter name='P_distRamif'>");
fprintf(FSeg,"%f",P_distRamif);
fprintf(FSeg,"</parameter>\n");

fprintf(FSeg,"      <parameter name='P_propDiamRamif'>");
fprintf(FSeg,"%f",P_propDiamRamif);
fprintf(FSeg,"</parameter>\n");

fprintf(FSeg,"      <parameter name='P_coeffVarDiamRamif'>");
fprintf(FSeg,"%f",P_coeffVarDiamRamif);
fprintf(FSeg,"</parameter>\n");

fprintf(FSeg,"      <parameter name='P_probaMaxArret'>");
fprintf(FSeg,"%f",P_probaMaxArret);
fprintf(FSeg,"</parameter>\n");

fprintf(FSeg,"      <parameter name='P_probaEffetDiam'>");
fprintf(FSeg,"%f",P_probaEffetDiam);
fprintf(FSeg,"</parameter>\n");

fprintf(FSeg,"      <parameter name='P_TMD'>");
fprintf(FSeg,"%f",P_TMD);
fprintf(FSeg,"</parameter>\n");

fprintf(FSeg,"      <parameter name='P_penteDureeVieDiamTMD'>");
fprintf(FSeg,"%f",P_penteDureeVieDiamTMD);
fprintf(FSeg,"</parameter>\n");

fprintf(FSeg,"      <parameter name='P_coeffCroissRad'>");
fprintf(FSeg,"%f",P_coeffCroissRad);
fprintf(FSeg,"</parameter>\n");

fprintf(FSeg,"      <parameter name='P_angLat'>");
fprintf(FSeg,"%f",P_angLat);
fprintf(FSeg,"</parameter>\n");

fprintf(FSeg,"      <parameter name='P_tertiary'>");
fprintf(FSeg,"%i",P_tertiary);
fprintf(FSeg,"</parameter>\n");

fprintf(FSeg,"      <parameter name='P_type'>");
fprintf(FSeg,"%i",P_type);
fprintf(FSeg,"</parameter>\n");

fprintf(FSeg,"      <parameter name='P_maxLatAge'>");
fprintf(FSeg,"%f",P_maxLatAge);
fprintf(FSeg,"</parameter>\n");

fprintf(FSeg,"    </parameters>\n");  
fprintf(FSeg,"    <user>globet</user>\n");
fprintf(FSeg,"    <file-key>myimage</file-key>\n");
fprintf(FSeg,"    <property-definitions>\n");
fprintf(FSeg,"      <property-definition>\n");
fprintf(FSeg,"          <label>diameter</label>\n");
fprintf(FSeg,"            <type>float</type>\n");
fprintf(FSeg,"            <unit>cm</unit>\n");
fprintf(FSeg,"      </property-definition>\n");
fprintf(FSeg,"      <property-definition>\n");
fprintf(FSeg,"          <label>age</label>\n");
fprintf(FSeg,"            <type>int</type>\n");
fprintf(FSeg,"            <unit>day</unit>\n");
fprintf(FSeg,"      </property-definition>\n");
fprintf(FSeg,"    </property-definitions>\n");
fprintf(FSeg,"  </metadata>\n");
fprintf(FSeg,"  <scene>\n");
fprintf(FSeg,"    <plant>  \n");

}  /* Fonction imprimeSRSegmentsEntete */
/****************************************************************************/
/****************************************************************************/
void imprimeAxeSegments(pTAxe axe)
{   /* Imprime les segments de l'axe */

  pTSeg segCour;
      fprintf(FSeg,"        <geometry>  \n");
      fprintf(FSeg,"          <polyline>  \n");
      segCour=axe->premSeg;
      while (segCour!=NULL) {
        if (segCour->complet) imprimeSeg(segCour, false);
        segCour=segCour->suiv;
      }
      segCour=axe->dernSeg;
      if (segCour->complet) imprimeSeg(segCour, true);

      fprintf(FSeg,"          </polyline>  \n");
      fprintf(FSeg,"        </geometry>  \n");

      // Print the diameters
      fprintf(FSeg,"        <functions>  \n");
      fprintf(FSeg,"          <function name='diameter' domain='polyline'>  \n");
      segCour=axe->premSeg;
      while (segCour!=NULL) {
        if (segCour->complet) printDiam(segCour);
        segCour=segCour->suiv;
      }
      segCour=axe->dernSeg;
      if (segCour->complet) printDiam(segCour);
      fprintf(FSeg,"          </function>  \n");

      // Print the age
      fprintf(FSeg,"          <function name='age' domain='polyline'>  \n");
      segCour=axe->premSeg;
      while (segCour!=NULL) {
        if (segCour->complet) printAge(segCour);
        segCour=segCour->suiv;
      }
      segCour=axe->dernSeg;
      if (segCour->complet) printAge(segCour);
      fprintf(FSeg,"          </function>  \n");
      fprintf(FSeg,"        </functions>  \n");
}  /* Fonction imprimeAxeSegments */
/****************************************************************************/
/****************************************************************************/
void imprimeSRSegments(pTSysRac sR)
{  /* Imprime l'ensemble des segments du système racinaire */

  pTAxe axeCour;
  pTAxe axeCour1;
  pTAxe axeCour2;

  imprimeSRSegmentsEntete();

  axeCour=sR->premAxe;
  while (axeCour!=NULL)
  {

    // Print the primary axes
    if(axeCour->pere==NULL){

      // Count the number of segments
      pTSeg segCour;
      int count = 0;
      segCour=axeCour->premSeg;
      while (segCour!=NULL) {
        if (segCour->complet) count = count+1;
        segCour=segCour->suiv;
      }
      if(count > 1){
        fprintf(FSeg,"      <root ID='");
        fprintf(FSeg,"%li",axeCour->num);
        fprintf(FSeg,"' label='root' po:accession='PO:0009005'>  \n");
        imprimeAxeSegments(axeCour);

        // Print the secondary axes
        axeCour1=sR->premAxe;
        while (axeCour1!=NULL)
        {
          if(axeCour1->pere==axeCour){
            int count = 0;
            segCour=axeCour1->premSeg;
            while (segCour!=NULL) {
              if (segCour->complet) count = count+1;
              segCour=segCour->suiv;
            }
            if(count > 1){
              fprintf(FSeg,"      <root ID='");
              fprintf(FSeg,"%li",axeCour1->num);
              fprintf(FSeg,"' label='root' po:accession='PO:0009005'>  \n");
              imprimeAxeSegments(axeCour1);

              if(P_tertiary == 1){
                  // Print the tertiary axes
                  axeCour2=sR->premAxe;
                  while (axeCour2!=NULL)
                  {
                    if(axeCour2->pere==axeCour1){
                      int count2 = 0;
                      segCour=axeCour2->premSeg;
                      while (segCour!=NULL) {
                        if (segCour->complet) count2 = count2+1;
                        segCour=segCour->suiv;
                      }
                      if(count2 > 1){
                        fprintf(FSeg,"      <root ID='");
                        fprintf(FSeg,"%li",axeCour1->num);
                        fprintf(FSeg,"' label='root' po:accession='PO:0009005'>  \n");
                        imprimeAxeSegments(axeCour2);
                        fprintf(FSeg,"      </root>  \n");
                      }
                    }
                    axeCour2=axeCour2->suivant;
                  }
              }
              fprintf(FSeg,"      </root>  \n");
            }
          }
          axeCour1=axeCour1->suivant;
        }

        fprintf(FSeg,"      </root>  \n");
      }
    }
    axeCour=axeCour->suivant;
  }
  fprintf(FSeg,"    </plant>  \n");
  fprintf(FSeg,"  </scene>\n");
  fprintf(FSeg,"</rsml>\n");
}  /* Fonction imprimeSRSegments */
/****************************************************************************/
/****************************************************************************/
void calcResumeSR(pTSysRac sR)
{  /* Calcule les différentes variables résumées et écriture sur fichier */
/*
  calcTSatisMoySR(sR);
  calcLimitesSR(sR);
  if ((sR->tSatisMoy>0.6)&&(sR->longueur>150)) { // Si le système est assez grand et le taux de satisfaction moyen est suffisant
    calcVolProdSR(sR);
    translateSR(sR);
    initialiseTabSol();
    calcDistancesSR(sR);
    imprimeSRGlobal(sR);
  }
  else fprintf(FSynth,"NA NA NA NA NA NA NA NA NA NA NA\n"); // données manquantes
*/
}  /* Fonction calcResumeSR */
/****************************************************************************/
/****************************************************************************/
void fermeFichiers(void)
{
  fclose(FSeg);
  fclose(FPar);
  fclose(FSol);
  fclose(FVol);
  fclose(FDem);
//  fclose(FAudit);
//  fclose(FSynth);
//  fclose(FVox);
}  /* Fonction fermeFichiers */
/****************************************************************************/

void sacrificeSR(pTSysRac sR)
/*MODIFBEN : rend une racine (définie par le paramètre condemnedRoot) sénile et arrêtée à un temps défini par le paramètre P_sacrificeTime*/
{
pTAxe axeCour;
  /* écriture d'un array contenant les valeurs des racines primaires*/
  axeCour=sR->premAxe;
  std::vector<int> array(P_nbMaxPrim);
  while (axeCour!=NULL)
  {
    if (axeCour->pere != NULL)
    {
      if( axeCour->pointe->age > P_maxLatAge*dRandUnif2()) axeCour->pointe->senile=1;
    }
    axeCour=axeCour->suivant;
  }
}
/****************************************************************************/


int main(int argc, char *argv[])
{
// struct timeval tv;   // version linux
// printf("Je passe 1  \n");

if (argc>1)
     { orig[0]=atof(argv[1]); orig[1]=atof(argv[2]); orig[2]=atof(argv[3]);}
else { orig[0]=0.0; orig[1]=0.0; orig[2]=20.0; } // semence légèrement enterrée
orig[0]=50.0; orig[1]=50.0; orig[2]=20.0;
//  gettimeofday(&tv,NULL);
//  printf("%d\n",  tv.tv_usec);
//  srand(tv.tv_usec); /* Initialisation du générateur aléatoire, version linux */

  srand( (unsigned) time(NULL) ); /* Initialisation du générateur aléatoire, version windows */



ouvreFichiers();
// printf("Je passe 2  \n");

litParam();
// printf("Je passe 3  \n");


// printf("Je passe 4  \n");


 /*
  printf(" Durée : %4i \n",P_duree);
  printf(" Age Maturité Pointe : %16.5f \n",P_ageMaturitePointe);
  printf(" Vitesse d'emission des primaires : %16.5f \n",P_vitEmissionPrim); // Vitesse d'émission des primaires
  printf(" Nombre maximal de  primaires : %5i \n",P_nbMaxPrim);  // Nombre maximal de primaires
  printf(" Diamètre minimal : %16.5f \n",P_diamMin);  //  Diamètre minimal (mm)
  printf(" Diamètre maximal : %16.5f \n",P_diamMax);  // Diamètre maximal (mm)
  printf(" Pente vitesse de croissance diamètre : %16.5f \n",P_penteVitDiam);  //  Pente vitesse de croissance diamètre
  printf(" Type de tropisme : %2i \n",P_tendanceDirTropisme); //   Type de tropisme (gravi positif, exo, plagio)
  printf(" Intensité du tropisme : %16.5f \n",P_intensiteTropisme); //  Intensité du tropisme
  printf(" Distance inter-ramifications : %16.5f \n",P_distRamif); //  Distance inter-ramifications (mm)
  printf(" Pente de la relation entre diamètre des latérales : %16.5f \n",P_propDiamRamif); // Pente de la relation entre diamètre des latérales et de la porteuse
  printf(" Coefficient de variabilité des latérales : %16.5f \n",P_coeffVarDiamRamif); // Coefficient de variabilité des latérales
  printf(" Masse volumique des racines : %16.5f \n",P_TMD); // Masse volumique des racines
  printf(" Pente durée de vie : %16.5f \n",P_penteDureeVieDiamTMD); // Pente durée de vie
  printf(" Coefficient de croissance radiale : %16.5f \n",P_coeffCroissRad); // Coefficient de croissance radiale

*/

litSol();

 //printf("Je passe 1  ");

sR=initialiseSR(orig);

litVolumeMaxSR(sR);

// printf("Taille de TSeg: %4li \n",sizeof(TSeg));
// printf("Taille de TAxe: %4li \n",sizeof(TAxe));
// printf("Taille de TPointe: %4li \n",sizeof(TPointe));
// Sleep(5001);
int count=1;
int inc = 0;
while (temps < P_duree)
{
  temps=temps+deltaT;
  //printf("\nIteration %4li", count); /*MODIFBEN : monitoring*/

//   printf("Temps : %3i \n",temps);

  /* Emission des racines primaires */
  emissionPrimSR(sR);

  /* Emission des racines primaires */
  emissionTardSR(sR);

  /* Développement du système racinaire */
  developpeSR(sR);

  sacrificeSR(sR);


  /* Croissance radiale du système racinaire */
  croissanceRadialeSR(sR, P_coeffCroissRad);

  /* Mortalité du système racinaire */
  mortaliteSR(sR);



  inc = inc + 1;
  if((inc == 15 & temps > 5) || inc == P_duree) 
    {
      ouvreFichiersOutput(); //MODIFBEN : ouverture du fichier d'output à chaque T pour incrémenter le nom de fichier. La fonction est modifiée en conséquence
      imprimeSRSegments(sR);
      fclose(FSeg); //MODIFBEN : Passage de imprimeSRSegments dans la boucle de temps afin d'avoir un rsml par pas de  temps
      inc = 0;
    }
  count=count+1;
}

//  imprimeAudit();
delete sR;
fermeFichiers();

//sleep(1);  // pas possible sur linux
return 0;
}

