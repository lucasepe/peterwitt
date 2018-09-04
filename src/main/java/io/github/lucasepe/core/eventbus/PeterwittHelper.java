package io.github.lucasepe.core.eventbus;


public class PeterwittHelper {

  private final PeterWitt eventBus;


  public PeterWitt getEventBus() {
    return this.eventBus;
  }

  private PeterwittHelper() {
    this.eventBus = new PeterWitt();
  }

  private static class LazyHolder {
    static final PeterwittHelper INSTANCE = new PeterwittHelper();
  }

  public static PeterwittHelper getInstance() {
    return LazyHolder.INSTANCE;
  }
}
