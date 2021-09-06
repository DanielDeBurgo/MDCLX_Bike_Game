// Fill out your copyright notice in the Description page of Project Settings.

#include "BikePawn.h"
#include "EngineUtils.h"

// Sets default values
ABikePawn::ABikePawn()
{
 	// Set this pawn to call Tick() every frame.  You can turn this off to improve performance if you don't need it.
	PrimaryActorTick.bCanEverTick = true;

}

// Called when the game starts or when spawned
void ABikePawn::BeginPlay()
{
	Super::BeginPlay();
}

// Called every frame
void ABikePawn::Tick(float DeltaTime)
{
	Super::Tick(DeltaTime);
}

// Called to bind functionality to input
void ABikePawn::SetupPlayerInputComponent(UInputComponent* PlayerInputComponent)
{
	Super::SetupPlayerInputComponent(PlayerInputComponent);

}

